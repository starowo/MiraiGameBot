const NAME = "云顶之巢";
const MAX_PLAYER = 8;
const RULE = "《云顶之巢》v1.1\n" +
"本游戏基于数字蜂巢，请先熟悉数字蜂巢的算分规则再进行本游戏。\n" +
"!!!0号位不记分数，但是可以填在0号位，等效于删除该块\n" +
"!!!可以将块替换已经存在的块，只记最新的块得分\n" +
"\n" +
"每个人拥有150点血量，中途每一轮正常轮结束你会随机挑选一名对手（奇数名玩家时会有一位玩家对战镜像），比拼当前分数，较低者会减去分数之差的血量（镜像的血量不会影响玩家本体）。\n" +
"\n" +
"卡池1有3*3*3+1癞子共28种棋子，每种两张，正常轮从卡池1取牌。\n" +
"卡池2有3*3*3共27种棋子，每种两张，特殊轮从卡池2取牌。\n" +
"\n" +
"第一轮从“卡池2”选出等于人数枚棋子，然后每人随机分配一枚，填入蜂巢中。第8、15、22等轮（回合数+7）裁判会从“卡池2”选出等于(人数+1)枚棋子并且公示(若卡池2已空，则加入一批新棋子)，由血量低往高进行选择，如果血量相同，则先掉到该血量的玩家先进行选择，如果同时掉到该血量，由掉血之前血量低的玩家先进行选择。如果上述全部相同，由裁判随机选择顺序。其余轮次为正常轮，每轮从卡池1选出公共棋子，玩家选择一个位置进行放置。\n" +
"\n" +
"存活时间越久的玩家排名越高，同一回合死去的玩家按血量高排名，同一回合死去且血量相同的玩家按得分数高排名，都相等则排名相同。当卡牌发完还未结束游戏，血量高的排名高。" +
"\n" +
"每局会触发不同的特殊事件哦~\n";
const ALLOW_GROUP = TRUE;

let pieces = new ArrayList();
let pieces_public = new ArrayList();
let players = new ArrayList();
let public_players = new ArrayList();
let dead_players = new ArrayList();
let received = new HashSet();
let boards = new ArrayList();
let fought_list = new ArrayList();
let last;
let last2;
let min_round;
let current;
let current_player;
let current_public = new ArrayList();
let round = 0;
let state = 0;
let rd = new Random();
let seed = rand.nextLong();
rd = new Random(seed);
let special;
let test = false;
let dRate = 0.0;
let iRate;
let playerSize;
let foughtRound = 0;

let timer;

if (args.length == 0) {
    special = -1;
} else {
    special = parseInt(args[0]);
}

function start() {
    if (state != 0)
        return;
    init();
    state = 1;
    Collections.shuffle(players, rd);
    min_round = Math.floor((players.size() + 1) / 2) - 1;
    if (players.size() == 2)
        min_round = 0;
    for (const player of players) {
        let board = new Board();
        board.special = special;
        boards.put(player, board);
    }
    update();
    timer = new Timer(120, () => {
        return state == -1;
    });
    timer.start();
}

function init() {
    playerSize = players.size();
    dRate = Math.pow(Math.E, players.size() / 6.0) / Math.E;
    iRate = dRate;
    j = special == -1 ? rd.nextInt(100) : special;
    special = Math.floor(j / 12) + 1;
    switch (special) {
        case 1:
                room.send("本局特殊事件：调色盘——卡池中添加大量癞子");
                break;
            case 2:
                room.send("本局特殊事件：大的没了——卡池中没有9");
                break;
            case 3:
                room.send("本局特殊事件：大的要来了——卡池中没有1");
                break;
            case 4:
                room.send("本局特殊事件：两极分化——卡池中没有5");
                break;
            case 5:
                room.send("本局特殊事件：有1吗——每行1额外加12分");
                break;
            case 6:
                room.send("本局特殊事件：小透不算挂——提前公布下一轮的卡（右侧为下一轮）");
                break;
            case 7:
                room.send("本局特殊事件：天降恩泽——第一轮每人发一个癞子");
                break;
            case 100:
                room.send("本局特殊事件：传世经典——本局游戏采用传统数字蜂巢规则");
                break;
            default:
                room.send("本局特殊事件：无");
                break;
    }
    pieces.clear();
    newPieces(false);
}

function newPieces(inPublic) {
    pieces = new ArrayList();
    for (let i = 0; i < 2; i++) {
        if(special != 3) {
            pieces.add(new Piece(3, 1, 2));
            pieces.add(new Piece(3, 1, 6));
            pieces.add(new Piece(3, 1, 7));
            pieces.add(new Piece(4, 1, 2));
            pieces.add(new Piece(4, 1, 6));
            pieces.add(new Piece(4, 1, 7));
            pieces.add(new Piece(8, 1, 2));
            pieces.add(new Piece(8, 1, 6));
            pieces.add(new Piece(8, 1, 7));
        }
        if(special != 4) {
            pieces.add(new Piece(3, 5, 2));
            pieces.add(new Piece(3, 5, 6));
            pieces.add(new Piece(3, 5, 7));
            pieces.add(new Piece(4, 5, 2));
            pieces.add(new Piece(4, 5, 6));
            pieces.add(new Piece(4, 5, 7));
            pieces.add(new Piece(8, 5, 2));
            pieces.add(new Piece(8, 5, 6));
            pieces.add(new Piece(8, 5, 7));
        }
        if(special != 2) {
            pieces.add(new Piece(3, 9, 2));
            pieces.add(new Piece(3, 9, 6));
            pieces.add(new Piece(3, 9, 7));
            pieces.add(new Piece(4, 9, 2));
            pieces.add(new Piece(4, 9, 6));
            pieces.add(new Piece(4, 9, 7));
            pieces.add(new Piece(8, 9, 2));
            pieces.add(new Piece(8, 9, 6));
            pieces.add(new Piece(8, 9, 7));
        }
        if(!inPublic || special == 1)
            pieces.add(new Piece(0, 0, 0));
    }
    if(inPublic && special == 1) {
        pieces.add(new Piece(0, 0, 0));
    }
    Collections.shuffle(pieces, rand);
    if (!inPublic && special == 1) {
        pieces.add(rand.nextInt(pieces.size()), new Piece(0, 0, 0));
        pieces.add(rand.nextInt(18), new Piece(0, 0, 0));
        pieces.add(rand.nextInt(19), new Piece(0, 0, 0));
    }
    if (!inPublic && special == 100) {
        for (let i = 0; i < 20; i++) {
            if (pieces.get(i).directions[0] == 0) {
                break;
            }
            if (i == 19) {
                for (let j = 0; j < 20; j++) {
                    pieces.remove(0);
                }
            }
        }
    }
    if(inPublic) {
        pieces_public.addAll(pieces);
    }else {
        this.pieces.addAll(pieces);
    }
}