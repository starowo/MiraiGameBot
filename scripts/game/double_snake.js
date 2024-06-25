const NAME ="双蛇";
const RULE = `
原作：碎狂执

在8*8的棋盘上，双方轮流落子，直到下满棋盘或双方同意结束落子。
黑棋先手，落一颗子，随后双方轮流连续落两颗子。第一颗子之后，所有新的落子必须落在某一旧子(任何颜色)的临近四格内。

双方结束落子后要在各自颜色的子中划定两条“蛇”。
选定一颗自己的子，随后将其连接到上下左右四个方向的一个同色子，直至无法继续连接或不想继续。即为一条蛇。蛇的长度为连接的子的数量。
两条蛇中不能有重复的子，同一条蛇中也不能有重复的子。
双方都执行完毕后，将自己的两条蛇的长度相乘，乘积大者获胜，相同平局`;
const ALLOW_GROUP = true;
const MAX_PLAYER = 2;
const NEED_AT = false;

let players = [null, null];
let received = [false, false];
let timecard = [2, 2];
let board = []; // 8 * 8, 0 for empty, 1 for black, 2 for white

let last_move = [-1, -1];
let finish = [false, false]

let snakes = [[[], []], [[], []]];

for (let i = 0; i < 8; i++) {
    board.push([0, 0, 0, 0, 0, 0, 0, 0]);
}

let turn = 0;
let round = 0;
let real_round = 0;
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
    timer = new Timer(90, () => {
        return game_end;
    });

    timer.on(60, () => {
        room.send("剩余时间60秒");
    }, false);

    timer.on(30, () => {
        room.send("剩余时间30秒");
    }, false);

    timer.on(10, () => {
        room.send(timecard[turn] > 0 ? "剩余时间10秒，超时将自动使用加时卡" : "剩余时间10秒, 超时将自动判负");
    }, false);

    timer.on(0, () => {
        if (round > 0 && round < 34) {
            if (timecard[turn] > 0) {
                timer.count = 45;
                timecard[turn]--;
                room.send(players[turn].name + " 超时，已自动使用加时卡，时间增加45秒，剩余加时卡 " + timecard[turn]);
            } else {
                snakes[turn] = [[], []];
                snakes[1 - turn] = [[0], [0]];
                stop();
            }
        } else {
            stop();
        }
    }, false);
    round = 1;
    turn = 0;
    if (Math.random() < 0.5) {
        let temp = players[0];
        players[0] = players[1];
        players[1] = temp;
    }
    game_end = false;
    timer.start();
    sendImage();
    let msg = new MessageChainBuilder().append(new At(players[turn].id)).append(" 执黑先手，请选择一个位置落子").build();
    room.send(msg);
}

function input(player, msg, group, at) {
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
    if (index != turn && round < 34) {
        return at ? new MessageChainBuilder().append("现在不是你的回合").build() : null;
    }
    let message = msg.contentToString().replaceAll("@" + room.group.getBot().getId(), "").trim();
    let move = message.trim().toUpperCase();
    if (move == "加时") {
        if (timecard[index] > 0) {
            timecard[index]--;
            timer.add(45, true);
            return new MessageChainBuilder().append("已使用加时卡，增加45秒时间，剩余加时卡 " + timecard[index]).build();
        } else {
            return new MessageChainBuilder().append("加时卡不足").build();
        }
    }
    if (move == "投降" || move == "认输") {
        snakes[index] = [[], []];
        snakes[1 - index] = [[0], [0]];
        stop();
        return null;        
    }
    let moves = move.split(" ");
    if (round == 1) {
        if (moves.length != 1) {
            return at ? new MessageChainBuilder().append("第一回合只能落一颗子").build() : null;
        }
        move = moves[0];
        let coord = move_to_coord(move);
        let x = coord[1];
        let y = coord[0];
        if (x < 0 || x >= 8 || y < 0 || y >= 8) {
            return at ? new MessageChainBuilder().append("坐标不合法").build() : null;
        }
        board[y][x] = 1;
        turn = 1 - turn;
        round++;
        last_move[0] = y * 8 + x;
        sendImage();
        timer.reset(90, true);
        return new MessageChainBuilder().append("请 ").append(new At(players[turn].id)).append(" 落子").build();
    }
    if (round == 33) {
        if (moves.length != 1) {
            return at ? new MessageChainBuilder().append("最后一回合只能落一颗子").build() : null;
        }
        move = moves[0];
        let coord = move_to_coord(move);
        let x = coord[1];
        let y = coord[0];
        if (x < 0 || x >= 8 || y < 0 || y >= 8) {
            return at ? new MessageChainBuilder().append("坐标不合法").build() : null;
        }
        if (board[y][x] != 0) {
            return at ? new MessageChainBuilder().append("该位置已有子").build() : null;
        }
        board[y][x] = 1;
        round++;
        last_move[1] = y * 8 + x;
        sendImage();
        timer.reset(300, true);
        let result = new MessageChainBuilder().append("双方落子完毕，开始划定蛇，限时五分钟\n 请发送两条蛇的轨迹，例如【蛇1 A1右右下下左下右右上】,【蛇2 A7左左】\n画完后请发送【完成】").build();
        return result;
    }
    if (round > 1 && round < 33) {
        if (moves[0] == "PASS") {
            if (last_move[0] == -1 && last_move[1] == -1) {
                real_round = round;
                round = 34;
                timer.reset(300, true);
                let result = new MessageChainBuilder().append("双方均PASS，进入划定蛇阶段，限时五分钟\n 请发送两条蛇的轨迹，例如【蛇1 A1右右下下左下右右上】,【蛇2 A7左左】\n画完后请发送【完成】").build();
                return result;
            }
            turn = 1 - turn;
            last_move[0] = -1;
            last_move[1] = -1;
            timer.reset(90, true);
            return new MessageChainBuilder().append("请 ").append(new At(players[turn].id)).append(" 落子，若双方均PASS则结束落子，直接进入划定蛇阶段").build();
        }
        if (moves.length != 2) {
            return at ? new MessageChainBuilder().append("落子数量错误，应落两颗子").build() : null;
        }
        let move1 = moves[0];
        let move2 = moves[1];
        let coord1 = move_to_coord(move1);
        let coord2 = move_to_coord(move2);
        let x1 = coord1[1];
        let y1 = coord1[0];
        let x2 = coord2[1];
        let y2 = coord2[0];
        if (x1 < 0 || x1 >= 8 || y1 < 0 || y1 >= 8) {
            return at ? new MessageChainBuilder().append(`坐标 ${move1} 不合法`).build() : null;
        }
        if (x2 < 0 || x2 >= 8 || y2 < 0 || y2 >= 8) {
            return at ? new MessageChainBuilder().append(`坐标 ${move2} 不合法`).build() : null;
        }
        if (board[y1][x1] != 0) {
            return new MessageChainBuilder().append(`坐标 ${move1} 已有子`).build();
        }
        if (board[y2][x2] != 0 || (x1 == x2 && y1 == y2)) {
            return new MessageChainBuilder().append(`坐标 ${move2} 已有子`).build();
        }
        let valid = false;
        if (x1 > 0 && board[y1][x1 - 1] != 0) {
            valid = true;
        }
        if (!valid && y1 > 0 && board[y1 - 1][x1] != 0) {
            valid = true;
        }
        if (!valid && x1 < 7 && board[y1][x1 + 1] != 0) {
            valid = true;
        }
        if (!valid && y1 < 7 && board[y1 + 1][x1] != 0) {
            valid = true;
        }
        if (!valid) {
            return new MessageChainBuilder().append(`坐标 ${move1} 不合法，必须落在某一旧子的临近四格内`).build();
        }
        valid = (Math.abs(x1 - x2) == 1 && Math.abs(y1 - y2) == 0) || (Math.abs(x1 - x2) == 0 && Math.abs(y1 - y2) == 1);
        if (!valid && x2 > 0 && board[y2][x2 - 1] != 0) {
            valid = true;
        }
        if (!valid && y2 > 0 && board[y2 - 1][x2] != 0) {
            valid = true;
        }
        if (!valid && x2 < 7 && board[y2][x2 + 1] != 0) {
            valid = true;
        }
        if (!valid && y2 < 7 && board[y2 + 1][x2] != 0) {
            valid = true;
        }
        if (!valid) {
            return new MessageChainBuilder().append(`坐标 ${move2} 不合法，必须落在某一旧子的临近四格内`).build();
        }
        board[y1][x1] = turn + 1;
        board[y2][x2] = turn + 1;
        turn = 1 - turn;
        round++;
        last_move[0] = y1 * 8 + x1;
        last_move[1] = y2 * 8 + x2;
        sendImage();
        timer.reset(90, true);
        return new MessageChainBuilder().append("请 ").append(new At(players[turn].id)).append(" 落子").build();
    }
    if (round == 34) {
        if (finish[index]) {
            return at ? new MessageChainBuilder().append("您已经主动结束了划定蛇的阶段").build() : null;
        }
        if (moves[0] == "完成") {
            finish[index] = true;
            if (finish[0] && finish[1]) {
                stop();
            }
            let len1 = snakes[index][0].length;
            let len2 = snakes[index][1].length;
            let len = len1 * len2;
            return new MessageChainBuilder().append(`您结束了划定蛇的阶段，蛇1长度 ${len1}，蛇2长度 ${len2}，获得的分数为 ${len}`).build();
        }
        if (moves.length != 2) {
            return at ? new MessageChainBuilder().append("输入错误，请输入蛇的轨迹，例如【蛇1 A1右右下下左下右右上】").build() : null;
        }
        if (moves[0].charAt(0) == "蛇") {
            let snake_index = parseInt(moves[0].charAt(1));
            if (isNaN(snake_index) || snake_index < 1 || snake_index > 2) {
                return at ? new MessageChainBuilder().append("蛇的编号错误").build() : null;
            }
            let snake = moves[1] + "";
            let head = snake.substring(0, 2);
            let x = head.charCodeAt(0) - "A".charCodeAt(0);
            let y = parseInt(head.charAt(1));
            if (isNaN(y) || x < 0 || x >= 8 || y < 0 || y >= 8) {
                return at ? new MessageChainBuilder().append("蛇的头部坐标错误").build() : null;
            }
            if (board[y][x] != index + 1) {
                return new MessageChainBuilder().append("蛇头位置不是你的棋子").build();
            }
            if (snakes[index][2 - snake_index].includes(y * 8 + x)) {
                return new MessageChainBuilder().append("两条蛇中不能有重复的棋子").build();
            }
            let snake_arr = [y * 8 + x];
            let dirs = snake.substring(2).split("");
            for (let dir of dirs) {
                if (dir == "左") {
                    x--;
                } else if (dir == "右") {
                    x++;
                } else if (dir == "上") {
                    y--;
                } else if (dir == "下") {
                    y++;
                } else {
                    return new MessageChainBuilder().append("方向错误").build();
                }
                if (x < 0 || x >= 8 || y < 0 || y >= 8) {
                    return new MessageChainBuilder().append("蛇超出棋盘").build();
                }
                if (board[y][x] == 0) {
                    return new MessageChainBuilder().append("蛇中不能有空地").build();
                }
                if (board[y][x] != index + 1) {
                    return new MessageChainBuilder().append("你不能将对方的棋子划进自己的蛇").build();
                }
                if (snake_arr.includes(y * 8 + x)) {
                    return new MessageChainBuilder().append("蛇中不能有重复的棋子").build();
                }
                if (snakes[index][2 - snake_index].includes(y * 8 + x)) {
                    return new MessageChainBuilder().append("两条蛇中不能有重复的棋子").build();
                }
                snake_arr.push(y * 8 + x);
            }
            snakes[index][snake_index - 1] = snake_arr;
            sendImage();
            return new MessageChainBuilder().append(`蛇${snake_index} 划定成功，长度 ${snake_arr.length}`).build();
        }
    }
    return null;
}

function stop() {
    let result = new MessageChainBuilder().append("游戏结束\n");
    let r = real_round == 0 ? round : real_round;
    let exp = Math.floor(r * 2000 / 34);
    let score1 = snakes[0][0].length * snakes[0][1].length;
    let score2 = snakes[1][0].length * snakes[1][1].length;
    if (score1 == score2) {
        exp = Math.floor(exp / 2 * 1.5);
        result.append(new At(players[0].id)).append(` 得分: ${score1}, 经验: +${exp}`).append("\n");
        result.append(new At(players[1].id)).append(` 得分: ${score2}, 经验: +${exp}`).append("\n");
        players[0].data.addExp(exp);
        players[1].data.addExp(exp);
        result.append("平局!");
    } else if (score1 < score2) {
        if (!players[0].rank.banned() && !players[1].rank.banned()) {
            let avgRank = Math.floor((players[0].rank.scores.getOrDefault("双蛇", 1200) + players[1].rank.scores.getOrDefault("双蛇", 1200)) / 2);
            let rank1 = players[0].rank.process("双蛇", -1.0, avgRank);
            let rank2 = players[1].rank.process("双蛇", 1.0, avgRank);
            result.append(new At(players[0].id)).append(` 得分: ${score1}, 经验: +${Math.floor(exp / 2)}, rank分: ${rank1}`).append("\n");
            result.append(new At(players[1].id)).append(` 得分: ${score2}, 经验: +${exp}, rank分: +${rank2}`).append("\n");
            players[0].data.addExp(Math.floor(exp / 2));
            players[1].data.addExp(exp);
        } else {
            result.append(new At(players[0].id)).append(` 得分: ${score1}, 经验: +${Math.floor(exp / 2)}`).append("\n");
            result.append(new At(players[1].id)).append(` 得分: ${score2}, 经验: +${exp}`).append("\n");
            players[0].data.addExp(Math.floor(exp / 2));
            players[1].data.addExp(exp);
        }
        result.append("恭喜胜者 —— ").append(new At(players[1].id)).append("！");
    } else {
        if (!players[0].rank.banned() && !players[1].rank.banned()) {
            let avgRank = Math.floor((players[0].rank.scores.getOrDefault("双蛇", 1200) + players[1].rank.scores.getOrDefault("双蛇", 1200)) / 2);
            let rank1 = players[0].rank.process("双蛇", 1.0, avgRank);
            let rank2 = players[1].rank.process("双蛇", -1.0, avgRank);
            result.append(new At(players[0].id)).append(` 得分: ${score1}, 经验: +${exp}, rank分: +${rank1}`).append("\n");
            result.append(new At(players[1].id)).append(` 得分: ${score2}, 经验: +${Math.floor(exp / 2)}, rank分: ${rank2}`).append("\n");
            players[0].data.addExp(exp);
            players[1].data.addExp(Math.floor(exp / 2));
        } else {
            result.append(new At(players[0].id)).append(` 得分: ${score1}, 经验: +${exp}`).append("\n");
            result.append(new At(players[1].id)).append(` 得分: ${score2}, 经验: +${Math.floor(exp / 2)}`).append("\n");
            players[0].data.addExp(exp);
            players[1].data.addExp(Math.floor(exp / 2));
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

function sendImage() {
    let g = new ImageBuilder(9 * 48, 9 * 48, 2);
    //room.send("#breakpoint - draw board");
    g.setColor(new Color(216, 191, 129));
    g.fillRect(0, 0, 10 * 48, 10 * 48);
    let font = new Font("微软雅黑", Font.static.BOLD, 24);
    g.setFont(font);
    //room.send("#breakpoint - draw coordinate");
    g.setColor(new Color(107, 66, 29));
    let chars = "ABCDEFGH";
    for (let i = 0; i < 8; i++) {
        let x = chars.charAt(i);
        let w = g.stringWidth(x);
        let h = g.getHeight();
        let centerX = 48 * (i + 1) + (48 - w) / 2;
        let centerY = (48 - h) / 2 + g.getAscent();
        g.drawString(x, Math.floor(centerX), Math.floor(centerY));
    }
    for (let i = 0; i < 8; i++) {
        let w = g.stringWidth(i+"");
        let h = g.getHeight();
        let centerX = (48 - w) / 2;
        let centerY = 48 * (i + 1) + (48 - h) / 2 + g.getAscent();
        g.drawString(i + "", Math.floor(centerX), Math.floor(centerY));
    }
    // draw board, bold border 4px, inside 2px
    g.fillRect(48 + 24 - 2, 48 + 24 - 2, 7 * 48 + 4, 4);
    g.fillRect(48 + 24 - 2, 48 + 24 - 2, 4, 7 * 48 + 4);
    g.fillRect(48 + 24 - 2, 24 + 8 * 48 - 2, 7 * 48 + 4, 4);
    g.fillRect(24 + 8 * 48 - 2, 48 + 24 - 2, 4, 7 * 48 + 4);
    for (let i = 0; i < 7; i++) {
        for (let j = 0; j < 7; j++) {
            let x = 48 * (i + 1) + 23;
            let y = 48 * (j + 1) + 23;
            //room.send("#breakpoint - draw stones");
            g.setColor(new Color(107, 66, 29));
            g.fillRect(x, y, 48, 48);
            g.setColor(new Color(216, 191, 129));
            g.fillRect(x + 2, y + 2, 46, 46);
        }
    }
    // draw snake lines
    for (let i = 0; i < 2; i++) {
        for (let j = 0; j < 2; j++) {
            let snake = snakes[i][j];
            g.setColor(i == 0 ? Color.static.BLACK : Color.static.WHITE);
            for (let k = 1; k < snake.length; k++) {
                let y1 = 48 * (Math.floor(snake[k - 1] / 8) + 1) + 24;
                let x1 = 48 * (snake[k - 1] % 8 + 1) + 24;
                let y2 = 48 * (Math.floor(snake[k] / 8) + 1) + 24;
                let x2 = 48 * (snake[k] % 8 + 1) + 24;
                if (y1 == y2) {
                    if (x1 < x2) {
                        g.fillRect(x1 - 2, y1 - 4, 48 + 4, 8);
                    }
                    if (x1 > x2) {
                        g.fillRect(x2 - 2, y1 - 4, 48 + 4, 8);
                    }
                }
                if (x1 == x2) {
                    if (y1 < y2) {
                        g.fillRect(x1 - 4, y1 - 2, 8, 48 + 4);
                    }
                    if (y1 > y2) {
                        g.fillRect(x1 - 4, y2 - 2, 8, 48 + 4);
                    }
                }
            }
        }
    }
    // draw stones
    for (let i = 0; i < 8; i ++) {
        for (let j = 0; j < 8; j++) {
            let y = 48 * (i + 1) + 24;
            let x = 48 * (j + 1) + 24;
            if (board[i][j] == 1) {
                g.setColor(Color.static.BLACK);
                g.fillOval(x - 18, y - 18, 36, 36);
            } else if (board[i][j] == 2) {
                g.setColor(Color.static.WHITE);
                g.fillOval(x - 18, y - 18, 36, 36);
            }
        }
    }
    // draw cursors on last move
    g.setColor(new Color(237, 28, 36));
    if (last_move[0] != -1) {
        let coord = number_to_coord(last_move[0]);
        let y = 48 * (coord[0] + 1) + 24;
        let x = 48 * (coord[1] + 1) + 24;
        g.fillRect(x - 10, y - 2, 8, 4);
        g.fillRect(x + 2, y - 2, 8, 4);
        g.fillRect(x - 2, y - 10, 4, 8);
        g.fillRect(x - 2, y + 2, 4, 8);
    }
    if (last_move[1] != -1) {
        let coord = number_to_coord(last_move[1]);
        let y = 48 * (coord[0] + 1) + 24;
        let x = 48 * (coord[1] + 1) + 24;
        g.fillRect(x - 10, y - 2, 8, 4);
        g.fillRect(x + 2, y - 2, 8, 4);
        g.fillRect(x - 2, y - 10, 4, 8);
        g.fillRect(x - 2, y + 2, 4, 8);
    }
    let bytes = g.build();
    room.send(bytes);
}

function number_to_coord(move) {
    let x = move % 8;
    let y = Math.floor(move / 8);
    return [y, x];
}

function move_to_coord(move) {
    let x = move.charCodeAt(0) - "A".charCodeAt(0);
    let y = parseInt(move.charAt(1));
    if (isNaN(y)) {
        return [-1, -1];
    }
    return [y, x];
}