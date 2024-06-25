const NAME = "陨落双子星";
const RULE = `原作：碎狂执
双方每人初始有100点血量
每回合进行以下行为
双方玩家先主动扣除自己的血量(扣除血量为正整数)。之后若A玩家扣除的血量正好为B玩家的n倍(n为非1正整数)，则B玩家的血量再除以n，向上取整。
血量先小于等于0的人失败，另一方获胜。
若同时小于等于0则平局`;
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
        room.send("玩家 " + player.name + " 的排位信息被封禁，本局游戏不计rank分");
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
        room.send("剩余时间30秒");
    }, false);
    
    timer.on(10, () => {
        room.send("剩余时间10秒，请尽快操作，超时将自动判负");
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
            throw new Error("积分倍率必须为非负整数");
        }
    }
    if (players[0] == null || players[1] == null) {
        throw new Error("玩家数量不足");
    }
    players[0].data.creditInGame = bet * 100;
    players[1].data.creditInGame = bet * 100;
    round = 1;
    received = [false, false];
    game_end = false;
    timer.start();
    let msg = new MessageChainBuilder().append(new At(players[0].id)).append(" ").append(new At(players[1].id)).append("\n").append("游戏开始！").append("\n").append("请私聊我发送你要扣除的血量(正整数)").build();
    room.send(msg);
}

function input(player, message, group, at) {
    if (group && at) {
        return new MessageChainBuilder().append("请私聊操作").build();
    }
    if (round == 0) {
        throw new Error("游戏未开始");
    }
    if (game_end) {
        throw new Error("游戏已结束");
    }
    let index = -1;
    for (let i = 0; i < players.length; i++) {
        if (players[i].id == player.id) {
            index = i;
            break;
        }
    }
    if (index == -1) {
        throw new Error("玩家不在游戏中");
    }
    if (received[index]) {
        return new MessageChainBuilder().append("你已经发送过本轮的操作了").build();
    }
    let text = message.contentToString();
    let value = parseInt(text);
    if (isNaN(value) || value <= 0) {
        return new MessageChainBuilder().append("请输入合法的正整数").build();
    }
    if (value > hp[index]) {
        return new MessageChainBuilder().append("你的血量不足").build();
    }
    received[index] = true;
    values[index] = value;
    hp[index] -= value;
    if (received[0] && received[1]) {
        let round_result = new MessageChainBuilder().append("回合结束：\n");
        let a = values[0];
        let b = values[1];
        round_result.append(players[0].name).append(" ").append(""+values[0]).append(" 血量: ").append("" + (hp[0]+a)).append(" -> ").append("" + (hp[0]))

        if (b % a == 0 && b > a) {
            hp[0] = Math.ceil(hp[0] / (b / a));
            round_result.append(" -> ").append("" + (hp[0]));
        }
        round_result.append("\n").append(players[1].name).append(" ").append("" + (values[1])).append(" 血量: ").append("" + (hp[1]+b)).append(" -> ").append("" + (hp[1]));

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
            let msg = new MessageChainBuilder().append("第").append("" + (round)).append("回合").append("\n").append("请私聊我发送你要扣除的血量(正整数)").build();
            room.send(msg);
            timer.reset(60, true);
        }
    }
    return new MessageChainBuilder().append("设置成功").build();
}

function stop() {
    let result = new MessageChainBuilder().append("游戏结束\n");
    players[0].data.creditInGame = 0;
    players[1].data.creditInGame = 0;
    if (hp[0] <= 0 && hp[1] <= 0) {
        result.append(new At(players[0].id)).append(" 剩余血量: ").append("" + (hp[0])).append("\n");
        result.append(new At(players[1].id)).append(" 剩余血量: ").append("" + (hp[1])).append("\n");
        result.append("平局!");
    } else if (hp[0] <= 0) {
        players[0].data.credit -= bet * 100;
        players[1].data.credit += bet * 100;
        if (!players[0].rank.banned() && !players[1].rank.banned()) {
            let avgRank = Math.floor((players[0].rank.scores.getOrDefault("陨落双子星", 1200) + players[1].rank.scores.getOrDefault("陨落双子星", 1200)) / 2);
            let rank1 = players[0].rank.process("陨落双子星", -1.0, avgRank);
            let rank2 = players[1].rank.process("陨落双子星", 1.0, avgRank);
            result.append(new At(players[0].id)).append(" 剩余血量: ").append("" + (hp[0])).append(" 积分: -").append("" + (bet * 100)).append(" rank分: ").append("" + (rank1)).append("\n");
            result.append(new At(players[1].id)).append(" 剩余血量: ").append("" + (hp[1])).append(" 积分: +").append("" + (bet * 100)).append(" rank分: +").append("" + (rank2)).append("\n");
        } else {
            result.append(new At(players[0].id)).append(" 剩余血量: ").append("" + (hp[0])).append(" 积分: -").append("" + (bet * 100)).append("\n");
            result.append(new At(players[1].id)).append(" 剩余血量: ").append("" + (hp[1])).append(" 积分: +").append("" + (bet * 100)).append("\n");
        }
        result.append("恭喜胜者 —— ").append(new At(players[1].id)).append("！");
    } else {
        players[0].data.credit += bet * 100;
        players[1].data.credit -= bet * 100;
        if (!players[0].rank.banned() && !players[1].rank.banned()) {
            let avgRank = Math.floor((players[0].rank.scores.getOrDefault("陨落双子星", 1200) + players[1].rank.scores.getOrDefault("陨落双子星", 1200)) / 2);
            let rank1 = players[0].rank.process("陨落双子星", 1.0, avgRank);
            let rank2 = players[1].rank.process("陨落双子星", -1.0, avgRank);
            result.append(new At(players[0].id)).append(" 剩余血量: ").append("" + (hp[0])).append(" 积分: +").append("" + (bet * 100)).append(" rank分: +").append("" + (rank1)).append("\n");
            result.append(new At(players[1].id)).append(" 剩余血量: ").append("" + (hp[1])).append(" 积分: -").append("" + (bet * 100)).append(" rank分: ").append("" + (rank2)).append("\n");
        } else {
            result.append(new At(players[0].id)).append(" 剩余血量: ").append("" + (hp[0])).append(" 积分: +").append("" + (bet * 100)).append("\n");
            result.append(new At(players[1].id)).append(" 剩余血量: ").append("" + (hp[1])).append(" 积分: -").append("" + (bet * 100)).append("\n");
        }
        result.append("恭喜胜者 —— ").append(new At(players[0].id)).append("！");
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