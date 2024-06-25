const NAME = "����˫����";
const RULE = `ԭ�������ִ
˫��ÿ�˳�ʼ��100��Ѫ��
ÿ�غϽ���������Ϊ
˫������������۳��Լ���Ѫ��(�۳�Ѫ��Ϊ������)��֮����A��ҿ۳���Ѫ������ΪB��ҵ�n��(nΪ��1������)����B��ҵ�Ѫ���ٳ���n������ȡ����
Ѫ����С�ڵ���0����ʧ�ܣ���һ����ʤ��
��ͬʱС�ڵ���0��ƽ��`;
const ALLOW_GROUP = false;
const MAX_PLAYER = 2;

let players = [null, null];
let received = [false, false];
let values = [0, 0];
let hp = [100, 100];
let round = 0;
let game_end = false;
let timer;

let bet = 0;

function addPlayer(player) {
    if (player.rank.banned()) {
        room.send("��� " + player.name + " ����λ��Ϣ�������������Ϸ����rank��");
    }
    for (let i = 0; i < players.length; i++) {
        if (players[i] == null) {
            players[i] = player;
            break;
        }
    }
}

function remove(player) {
    for (let i = 0; i < players.length; i++) {
        if (players[i].id == player.id) {
            players[i] = null;
            break;
        }
    }
}

function isWaiting() {
    return round == 0;
}

function start() {
    timer = new Timer(60, () => {
        return game_end;
    });
    timer.on(30, () => {
        room.send("ʣ��ʱ��30��");
    }, false);
    
    timer.on(10, () => {
        room.send("ʣ��ʱ��10�룬�뾡���������ʱ���Զ��и�");
    }, false);
    
    timer.on(0, () => {
        if (round > 0) {
            if (!received[0]) {
                hp[0] = 0;
            }
            if (!received[1]) {
                hp[1] = 0;
            }
            stop();
        } else {
            game_end = true;
        }
    }, false);
    bet = 10;
    let args_field = game.getClass().getDeclaredField("args");
    args_field.setAccessible(true);
    let args = args_field.get(game);
    if (args.length > 0) {
        bet = parseInt(args[0]);
        if (isNaN(bet) || bet < 0) {
            throw new Error("���ֱ��ʱ���Ϊ�Ǹ�����");
        }
    }
    if (players[0] == null || players[1] == null) {
        throw new Error("�����������");
    }
    players[0].data.creditInGame = bet * 100;
    players[1].data.creditInGame = bet * 100;
    round = 1;
    received = [false, false];
    game_end = false;
    timer.start();
    let msg = new MessageChainBuilder().append(new At(players[0].id)).append(" ").append(new At(players[1].id)).append("\n").append("��Ϸ��ʼ��").append("\n").append("��˽���ҷ�����Ҫ�۳���Ѫ��(������)").build();
    room.send(msg);
}

function input(player, message, group, at) {
    if (group && at) {
        return new MessageChainBuilder().append("��˽�Ĳ���").build();
    }
    if (round == 0) {
        throw new Error("��Ϸδ��ʼ");
    }
    if (game_end) {
        throw new Error("��Ϸ�ѽ���");
    }
    let index = -1;
    for (let i = 0; i < players.length; i++) {
        if (players[i].id == player.id) {
            index = i;
            break;
        }
    }
    if (index == -1) {
        throw new Error("��Ҳ�����Ϸ��");
    }
    if (received[index]) {
        return new MessageChainBuilder().append("���Ѿ����͹����ֵĲ�����").build();
    }
    let text = message.contentToString();
    let value = parseInt(text);
    if (isNaN(value) || value <= 0) {
        return new MessageChainBuilder().append("������Ϸ���������").build();
    }
    if (value > hp[index]) {
        return new MessageChainBuilder().append("���Ѫ������").build();
    }
    received[index] = true;
    values[index] = value;
    hp[index] -= value;
    if (received[0] && received[1]) {
        let round_result = new MessageChainBuilder().append("�غϽ�����\n");
        let a = values[0];
        let b = values[1];
        round_result.append(players[0].name).append(" ").append(""+values[0]).append(" Ѫ��: ").append("" + (hp[0]+a)).append(" -> ").append("" + (hp[0]))

        if (b % a == 0 && b > a) {
            hp[0] = Math.ceil(hp[0] / (b / a));
            round_result.append(" -> ").append("" + (hp[0]));
        }
        round_result.append("\n").append(players[1].name).append(" ").append("" + (values[1])).append(" Ѫ��: ").append("" + (hp[1]+b)).append(" -> ").append("" + (hp[1]));

        if (a % b == 0 && a > b) {
            hp[1] = Math.ceil(hp[1] / (a / b));
            round_result.append(" -> ").append("" + (hp[1]));
        }

        room.send(round_result.build());

        if (hp[0] <= 0 || hp[1] <= 0) {
            stop();
        } else {
            round++;
            received = [false, false];
            let msg = new MessageChainBuilder().append("��").append("" + (round)).append("�غ�").append("\n").append("��˽���ҷ�����Ҫ�۳���Ѫ��(������)").build();
            room.send(msg);
            timer.reset(60, true);
        }
    }
    return new MessageChainBuilder().append("���óɹ�").build();
}

function stop() {
    let result = new MessageChainBuilder().append("��Ϸ����\n");
    players[0].data.creditInGame = 0;
    players[1].data.creditInGame = 0;
    if (hp[0] <= 0 && hp[1] <= 0) {
        result.append(new At(players[0].id)).append(" ʣ��Ѫ��: ").append("" + (hp[0])).append("\n");
        result.append(new At(players[1].id)).append(" ʣ��Ѫ��: ").append("" + (hp[1])).append("\n");
        result.append("ƽ��!");
    } else if (hp[0] <= 0) {
        players[0].data.credit -= bet * 100;
        players[1].data.credit += bet * 100;
        if (!players[0].rank.banned() && !players[1].rank.banned()) {
            let avgRank = Math.floor((players[0].rank.scores.getOrDefault("����˫����", 1200) + players[1].rank.scores.getOrDefault("����˫����", 1200)) / 2);
            let rank1 = players[0].rank.process("����˫����", -1.0, avgRank);
            let rank2 = players[1].rank.process("����˫����", 1.0, avgRank);
            result.append(new At(players[0].id)).append(" ʣ��Ѫ��: ").append("" + (hp[0])).append(" ����: -").append("" + (bet * 100)).append(" rank��: ").append("" + (rank1)).append("\n");
            result.append(new At(players[1].id)).append(" ʣ��Ѫ��: ").append("" + (hp[1])).append(" ����: +").append("" + (bet * 100)).append(" rank��: +").append("" + (rank2)).append("\n");
        } else {
            result.append(new At(players[0].id)).append(" ʣ��Ѫ��: ").append("" + (hp[0])).append(" ����: -").append("" + (bet * 100)).append("\n");
            result.append(new At(players[1].id)).append(" ʣ��Ѫ��: ").append("" + (hp[1])).append(" ����: +").append("" + (bet * 100)).append("\n");
        }
        result.append("��ϲʤ�� ���� ").append(new At(players[1].id)).append("��");
    } else {
        players[0].data.credit += bet * 100;
        players[1].data.credit -= bet * 100;
        if (!players[0].rank.banned() && !players[1].rank.banned()) {
            let avgRank = Math.floor((players[0].rank.scores.getOrDefault("����˫����", 1200) + players[1].rank.scores.getOrDefault("����˫����", 1200)) / 2);
            let rank1 = players[0].rank.process("����˫����", 1.0, avgRank);
            let rank2 = players[1].rank.process("����˫����", -1.0, avgRank);
            result.append(new At(players[0].id)).append(" ʣ��Ѫ��: ").append("" + (hp[0])).append(" ����: +").append("" + (bet * 100)).append(" rank��: +").append("" + (rank1)).append("\n");
            result.append(new At(players[1].id)).append(" ʣ��Ѫ��: ").append("" + (hp[1])).append(" ����: -").append("" + (bet * 100)).append(" rank��: ").append("" + (rank2)).append("\n");
        } else {
            result.append(new At(players[0].id)).append(" ʣ��Ѫ��: ").append("" + (hp[0])).append(" ����: +").append("" + (bet * 100)).append("\n");
            result.append(new At(players[1].id)).append(" ʣ��Ѫ��: ").append("" + (hp[1])).append(" ����: -").append("" + (bet * 100)).append("\n");
        }
        result.append("��ϲʤ�� ���� ").append(new At(players[0].id)).append("��");
    }
    room.send(result.build());
    round = 0;
    game_end = true;
    room.delete();
    Manager.static.saveRank();
    Manager.static.save();
}

function canJoin(player) {
    return player.credit >= bet * 100;
}