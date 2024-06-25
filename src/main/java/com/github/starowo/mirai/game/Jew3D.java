package com.github.starowo.mirai.game;

import com.github.starowo.mirai.MiraiGamePlugin;
import com.github.starowo.mirai.player.GamePlayer;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.message.data.Message;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.MessageChainBuilder;
import net.mamoe.mirai.message.data.QuoteReply;
import net.mamoe.mirai.utils.ExternalResource;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

public class Jew3D implements IGame {

    public Group group;
    public int round;
    public GamePlayer player1;
    public GamePlayer player2;
    public int status = 0;
    public boolean[][][] board = new boolean[4][4][4];
    public ArrayList<boolean[][][]> history = new ArrayList<>();
    public boolean acting = new Random().nextBoolean();
    private boolean tryStop = false;

    public Jew3D(Group group) {
        this.group = group;
    }

    private static void drawCube(BufferedImage image, int x, int y, int a, boolean b) {
        Graphics2D g = image.createGraphics();
        g.setTransform(new AffineTransform(1, 0, -.5, 0.3, a * 0.5 + x, y - a * 1.6));
        g.setColor(b ? Color.black : Color.white);
        g.fillRect(0, 0, a, a);
        g.setColor(Color.GRAY);
        g.drawRect(0, 0, a, a);
        g.setTransform(new AffineTransform(1, 0, 0, -1, x, -a * 0.3 + y));
        g.setColor(b ? Color.black : Color.white);
        g.fillRect(0, 0, a, a);
        g.setColor(Color.GRAY);
        g.drawRect(0, 0, a, a);
        g.setTransform(new AffineTransform(0.5, -0.3, 0, -1, a + x, -a * 0.3 + y));
        g.setColor(b ? Color.black : Color.white);
        g.fillRect(0, 0, a, a);
        g.setColor(Color.GRAY);
        g.drawRect(0, 0, a, a);
        g.dispose();
    }

    @Override
    public void start() {
        status = 1;
        update();
    }

    @Override
    public Message input(GamePlayer player, MessageChain msg) {
        String text = msg.contentToString().replaceAll("@" + group.getBot().getId(), "").trim();
        if (text.equals("stop")) {
            if (tryStop) {
                stop();
                return null;
            }
            tryStop = true;
            return new MessageChainBuilder().append("再次输入 \"stop\" 以确认中止游戏").build();
        }
        if (tryStop) {
            group.sendMessage(new MessageChainBuilder().append("已取消中止游戏").build());
            tryStop = false;
        }
        if (text.startsWith("back")) {
            String r = text.replaceAll("back", "").trim();
            try {
                int b = Integer.parseInt(r);
                if ((round - b) % 2 == 0) {
                    acting = !acting;
                }
                board = history.get(b - 1);
                while (history.size() > b - 1) {
                    history.remove(b - 1);
                }
                round = b - 1;
                update();
                return null;
            } catch (Throwable e) {
                return new MessageChainBuilder().append("回退失败").build();
            }
        }
        if ((player.equals(player1) && !acting) || player.equals(player2) && acting) {
            return new MessageChainBuilder()
                    .append("现在不是你的回合哦~")
                    .append(new QuoteReply(msg))
                    .build();
        }
        boolean plane = false;
        if (text.startsWith("面")) {
            text = text.replaceAll("面", "").trim();
            plane = true;
        }
        if (text.length() != 6 && text.length() != 3) {
            return new MessageChainBuilder().append("格式错误").build();
        }
        if (!plane) {
            int x = -1;
            int y = -1;
            int z = -1;
            for (int i = 0; i < 3; i++) {
                char c = text.charAt(i);
                switch (c) {
                    case 'a':
                        x = 0;
                        break;
                    case 'b':
                        x = 1;
                        break;
                    case 'c':
                        x = 2;
                        break;
                    case 'd':
                        x = 3;
                        break;
                    case 'w':
                        z = 0;
                        break;
                    case 'x':
                        z = 1;
                        break;
                    case 'y':
                        z = 2;
                        break;
                    case 'z':
                        z = 3;
                        break;
                    case '1':
                        y = 0;
                        break;
                    case '2':
                        y = 1;
                        break;
                    case '3':
                        y = 2;
                        break;
                    case '4':
                        y = 3;
                        break;
                }
            }
            if (x < 0 || y < 0 || z < 0) {
                return new MessageChainBuilder().append("格式错误").build();
            }
            int x1 = x;
            int y1 = y;
            int z1 = z;
            if (text.length() == 6) {
                x1 = -1;
                y1 = -1;
                z1 = -1;
                for (int i = 3; i < 6; i++) {
                    char c = text.charAt(i);
                    switch (c) {
                        case 'a':
                            x1 = 0;
                            break;
                        case 'b':
                            x1 = 1;
                            break;
                        case 'c':
                            x1 = 2;
                            break;
                        case 'd':
                            x1 = 3;
                            break;
                        case 'w':
                            z1 = 0;
                            break;
                        case 'x':
                            z1 = 1;
                            break;
                        case 'y':
                            z1 = 2;
                            break;
                        case 'z':
                            z1 = 3;
                            break;
                        case '1':
                            y1 = 0;
                            break;
                        case '2':
                            y1 = 1;
                            break;
                        case '3':
                            y1 = 2;
                            break;
                        case '4':
                            y1 = 3;
                            break;
                    }
                }
                if (x1 < 0 || y1 < 0 || z1 < 0) {
                    return new MessageChainBuilder().append("格式错误").build();
                }
            }
            int dx = Math.abs(x - x1);
            int dy = Math.abs(y - y1);
            int dz = Math.abs(z - z1);
            if (dx == dy && dy == dz || dy == 0 && (dx == dz || dx == dy || dy == dz) || dz == 0 && (dx == dz || dx == dy) || dx == 0 && dy == dz) {
                int x2 = x;
                int y2 = y;
                int z2 = z;
                int max = Math.max(Math.max(dx, dy), dz);
                for (int i = 0; i <= max; i++) {
                    if (board[x2][y2][z2]) {
                        return new MessageChainBuilder().append("路径被阻挡").build();
                    }
                    if (x2 < x1) {
                        x2++;
                    } else if (x2 > x1) {
                        x2--;
                    }
                    if (y2 < y1) {
                        y2++;
                    } else if (y2 > y1) {
                        y2--;
                    }
                    if (z2 < z1) {
                        z2++;
                    } else if (z2 > z1) {
                        z2--;
                    }
                }
                x2 = x;
                y2 = y;
                z2 = z;
                for (int i = 0; i <= max; i++) {
                    board[x2][y2][z2] = true;
                    if (x2 < x1) {
                        x2++;
                    } else if (x2 > x1) {
                        x2--;
                    }
                    if (y2 < y1) {
                        y2++;
                    } else if (y2 > y1) {
                        y2--;
                    }
                    if (z2 < z1) {
                        z2++;
                    } else if (z2 > z1) {
                        z2--;
                    }
                }
            } else {
                return new MessageChainBuilder().append("路径不合法").build();
            }
        } else {
            int x = -1;
            int y = -1;
            int z = -1;
            for (int i = 0; i < 3; i++) {
                char c = text.charAt(i);
                switch (c) {
                    case 'a':
                        x = 0;
                        break;
                    case 'b':
                        x = 1;
                        break;
                    case 'c':
                        x = 2;
                        break;
                    case 'd':
                        x = 3;
                        break;
                    case 'w':
                        z = 0;
                        break;
                    case 'x':
                        z = 1;
                        break;
                    case 'y':
                        z = 2;
                        break;
                    case 'z':
                        z = 3;
                        break;
                    case '1':
                        y = 0;
                        break;
                    case '2':
                        y = 1;
                        break;
                    case '3':
                        y = 2;
                        break;
                    case '4':
                        y = 3;
                        break;
                }
            }
            if (x < 0 || y < 0 || z < 0) {
                return new MessageChainBuilder().append("格式错误").build();
            }
            int x1 = x;
            int y1 = y;
            int z1 = z;
            if (text.length() == 6) {
                x1 = -1;
                y1 = -1;
                z1 = -1;
                for (int i = 3; i < 6; i++) {
                    char c = text.charAt(i);
                    switch (c) {
                        case 'a':
                            x1 = 0;
                            break;
                        case 'b':
                            x1 = 1;
                            break;
                        case 'c':
                            x1 = 2;
                            break;
                        case 'd':
                            x1 = 3;
                            break;
                        case 'w':
                            z1 = 0;
                            break;
                        case 'x':
                            z1 = 1;
                            break;
                        case 'y':
                            z1 = 2;
                            break;
                        case 'z':
                            z1 = 3;
                            break;
                        case '1':
                            y1 = 0;
                            break;
                        case '2':
                            y1 = 1;
                            break;
                        case '3':
                            y1 = 2;
                            break;
                        case '4':
                            y1 = 3;
                            break;
                    }
                }
                if (x1 < 0 || y1 < 0 || z1 < 0) {
                    return new MessageChainBuilder().append("格式错误").build();
                }
            }
            int dx = Math.abs(x - x1);
            int dy = Math.abs(y - y1);
            int dz = Math.abs(z - z1);
            if ((dx == 0 && dy > 0 && dz > 0) || (dy > 0 && dx > 0 && dz == 0) || (dy == 0 && dz > 0 && dx > 0)) {
                int x2;
                int y2;
                int z2 = z;
                int max = Math.max(Math.max(dx, dy), dz);
                for (int i = 0; i <= dz; i++) {
                    y2 = y;
                    for (int j = 0; j <= dy; j++) {
                        x2 = x;
                        for (int k = 0; k <= dx; k++) {
                            if (board[x2][y2][z2]) {
                                return new MessageChainBuilder().append("路径被阻挡").build();
                            }
                            if (x2 < x1) {
                                x2++;
                            } else if (x2 > x1) {
                                x2--;
                            }
                        }
                        if (y2 < y1) {
                            y2++;
                        } else if (y2 > y1) {
                            y2--;
                        }
                    }

                    if (z2 < z1) {
                        z2++;
                    } else if (z2 > z1) {
                        z2--;
                    }
                }
                z2 = z;
                for (int i = 0; i <= dz; i++) {
                    y2 = y;
                    for (int j = 0; j <= dy; j++) {
                        x2 = x;
                        for (int k = 0; k <= dx; k++) {
                            board[x2][y2][z2] = true;
                            if (x2 < x1) {
                                x2++;
                            } else if (x2 > x1) {
                                x2--;
                            }
                        }
                        if (y2 < y1) {
                            y2++;
                        } else if (y2 > y1) {
                            y2--;
                        }
                    }

                    if (z2 < z1) {
                        z2++;
                    } else if (z2 > z1) {
                        z2--;
                    }
                }
            } else {
                return new MessageChainBuilder().append("路径不合法").build();
            }
        }
        update();
        return null;
    }

    private void update() {
        acting = !acting;
        round++;
        boolean[][][] clone = new boolean[4][4][4];
        for (int x = 0; x < 4; x++) {
            for (int y = 0; y < 4; y++) {
                System.arraycopy(board[x][y], 0, clone[x][y], 0, 4);
            }
        }
        history.add(clone);
        GamePlayer player = acting ? player1 : player2;
        group.sendMessage(new MessageChainBuilder().append("Round").append(String.valueOf(round)).append("\n")
                .append(player.name)
                .append(" 的回合")
                .build());
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        byte[] baImage = null;
        try {
            ImageIO.write(getImage(), "png", os);
            baImage = os.toByteArray();
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (baImage != null) {
            ExternalResource resource = ExternalResource.create(baImage);
            group.sendMessage(group.uploadImage(resource));
            try {
                resource.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private BufferedImage getImage() {
        BufferedImage image = new BufferedImage(600, 500, BufferedImage.TYPE_INT_RGB);
        Graphics2D gr = image.createGraphics();
        gr.setColor(Color.LIGHT_GRAY);
        gr.fillRect(0, 0, 600, 500);
        for (int k = 0; k < 4; k++) {
            for (int j = 0; j < 4; j++) {
                for (int i = 0; i < 4; i++) {
                    drawCube(image, (int) (30 + i * 20 - k * 0.5 * 20), (int) (-.9 * 20 + 150 - j * 20 + k * 0.3 * 20), 20, board[i][j][k]);
                }
            }
        }
        for (int k = 0; k < 3; k++) {
            for (int j = 0; j < 4; j++) {
                for (int i = 0; i < 4; i++) {
                    drawCube(image, (int) (170 + i * 20 - k * 0.5 * 20), (int) (-.9 * 20 + 150 - j * 20 + k * 0.3 * 20), 20, board[i][j][k]);
                }
            }
        }
        for (int k = 0; k < 2; k++) {
            for (int j = 0; j < 4; j++) {
                for (int i = 0; i < 4; i++) {
                    drawCube(image, (int) (310 + i * 20 - k * 0.5 * 20), (int) (-.9 * 20 + 150 - j * 20 + k * 0.3 * 20), 20, board[i][j][k]);
                }
            }
        }
        for (int k = 0; k < 1; k++) {
            for (int j = 0; j < 4; j++) {
                for (int i = 0; i < 4; i++) {
                    drawCube(image, (int) (450 + i * 20 - k * 0.5 * 20), (int) (-.9 * 20 + 150 - j * 20 + k * 0.3 * 20), 20, board[i][j][k]);
                }
            }
        }
        for (int k = 0; k < 4; k++) {
            for (int j = 0; j < 4; j++) {
                for (int i = 0; i < 4; i++) {
                    if (board[i][j][k])
                        drawCube(image, (int) (30 + i * 20 - k * 0.5 * 20), (int) (-.9 * 20 + 280 - j * 20 + k * 0.3 * 20), 20, board[i][j][k]);
                }
            }
        }
        for (int k = 0; k < 4; k++) {
            for (int j = 0; j < 3; j++) {
                for (int i = 0; i < 4; i++) {
                    drawCube(image, (int) (170 + i * 20 - k * 0.5 * 20), (int) (-.9 * 20 + 280 - j * 20 + k * 0.3 * 20), 20, board[i][j][k]);
                }
            }
        }
        for (int k = 0; k < 4; k++) {
            for (int j = 0; j < 2; j++) {
                for (int i = 0; i < 4; i++) {
                    drawCube(image, (int) (310 + i * 20 - k * 0.5 * 20), (int) (-.9 * 20 + 280 - j * 20 + k * 0.3 * 20), 20, board[i][j][k]);
                }
            }
        }
        for (int k = 0; k < 4; k++) {
            for (int j = 0; j < 1; j++) {
                for (int i = 0; i < 4; i++) {
                    drawCube(image, (int) (450 + i * 20 - k * 0.5 * 20), (int) (-.9 * 20 + 280 - j * 20 + k * 0.3 * 20), 20, board[i][j][k]);
                }
            }
        }
        Graphics2D gr1 = image.createGraphics();
        gr1.setColor(Color.black);
        for (int k = 0; k < 4; k++) {
            for (int j = 0; j < 4; j++) {
                for (int i = 0; i < 4; i++) {
                    if (board[i][j][k]) {
                        gr1.fillRect(20 + 30 * i, 390 - 30 * j + k * 5, 21, 5);
                    } else {
                        gr1.drawRect(20 + 30 * i, 390 - 30 * j + k * 5, 20, 5);
                    }
                }
            }
        }
        gr1.dispose();
        for (int k = 0; k < 4; k++) {
            for (int j = 0; j < 4; j++) {
                for (int i = 0; i < 3; i++) {
                    drawCube(image, (int) (170 + i * 20 - k * 0.5 * 20), (int) (-.9 * 20 + 410 - j * 20 + k * 0.3 * 20), 20, board[i][j][k]);
                }
            }
        }
        for (int k = 0; k < 4; k++) {
            for (int j = 0; j < 4; j++) {
                for (int i = 0; i < 2; i++) {
                    drawCube(image, (int) (310 + i * 20 - k * 0.5 * 20), (int) (-.9 * 20 + 410 - j * 20 + k * 0.3 * 20), 20, board[i][j][k]);
                }
            }
        }
        for (int k = 0; k < 4; k++) {
            for (int j = 0; j < 4; j++) {
                for (int i = 0; i < 1; i++) {
                    drawCube(image, (int) (450 + i * 20 - k * 0.5 * 20), (int) (-.9 * 20 + 410 - j * 20 + k * 0.3 * 20), 20, board[i][j][k]);
                }
            }
        }
        gr.setColor(Color.black);
        gr.drawString("a", 45, 35);
        gr.drawString("b", 65, 35);
        gr.drawString("c", 85, 35);
        gr.drawString("d", 105, 35);
        gr.drawString("4", 125, 55);
        gr.drawString("3", 125, 75);
        gr.drawString("2", 125, 95);
        gr.drawString("1", 125, 115);
        gr.drawString("w", 113, 135);
        gr.drawString("x", 104, 140);
        gr.drawString("y", 95, 145);
        gr.drawString("z", 85, 150);

        gr.drawString("a", 185, 35);
        gr.drawString("b", 205, 35);
        gr.drawString("c", 225, 35);
        gr.drawString("d", 245, 35);
        gr.drawString("4", 265, 55);
        gr.drawString("3", 265, 75);
        gr.drawString("2", 265, 95);
        gr.drawString("1", 265, 115);
        gr.drawString("w", 253, 135);
        gr.drawString("x", 244, 140);
        gr.drawString("y", 235, 145);

        gr.drawString("a", 325, 35);
        gr.drawString("b", 345, 35);
        gr.drawString("c", 365, 35);
        gr.drawString("d", 385, 35);
        gr.drawString("4", 405, 55);
        gr.drawString("3", 405, 75);
        gr.drawString("2", 405, 95);
        gr.drawString("1", 405, 115);
        gr.drawString("w", 393, 135);
        gr.drawString("x", 384, 140);

        gr.drawString("a", 465, 35);
        gr.drawString("b", 485, 35);
        gr.drawString("c", 505, 35);
        gr.drawString("d", 525, 35);
        gr.drawString("4", 545, 55);
        gr.drawString("3", 545, 75);
        gr.drawString("2", 545, 95);
        gr.drawString("1", 545, 115);
        gr.drawString("w", 533, 135);

        gr.drawString("a", 45, 165);
        gr.drawString("b", 65, 165);
        gr.drawString("c", 85, 165);
        gr.drawString("d", 105, 165);
        gr.drawString("4", 125, 185);
        gr.drawString("3", 125, 205);
        gr.drawString("2", 125, 225);
        gr.drawString("1", 125, 245);
        gr.drawString("w", 113, 265);
        gr.drawString("x", 104, 270);
        gr.drawString("y", 95, 275);
        gr.drawString("z", 86, 280);

        gr.drawString("a", 185, 185);
        gr.drawString("b", 205, 185);
        gr.drawString("c", 225, 185);
        gr.drawString("d", 245, 185);
        gr.drawString("3", 265, 205);
        gr.drawString("2", 265, 225);
        gr.drawString("1", 265, 245);
        gr.drawString("w", 253, 265);
        gr.drawString("x", 244, 270);
        gr.drawString("y", 235, 275);
        gr.drawString("z", 226, 280);

        gr.drawString("a", 325, 205);
        gr.drawString("b", 345, 205);
        gr.drawString("c", 365, 205);
        gr.drawString("d", 385, 205);
        gr.drawString("2", 405, 225);
        gr.drawString("1", 405, 245);
        gr.drawString("w", 393, 265);
        gr.drawString("x", 384, 270);
        gr.drawString("y", 375, 275);
        gr.drawString("z", 366, 280);

        gr.drawString("a", 465, 225);
        gr.drawString("b", 485, 225);
        gr.drawString("c", 505, 225);
        gr.drawString("d", 525, 225);
        gr.drawString("1", 545, 245);
        gr.drawString("w", 533, 265);
        gr.drawString("x", 524, 270);
        gr.drawString("y", 515, 275);
        gr.drawString("z", 506, 280);

        gr.drawString("a", 185, 295);
        gr.drawString("b", 205, 295);
        gr.drawString("c", 225, 295);
        gr.drawString("4", 245, 315);
        gr.drawString("3", 245, 335);
        gr.drawString("2", 245, 355);
        gr.drawString("1", 245, 375);
        gr.drawString("w", 233, 395);
        gr.drawString("x", 224, 400);
        gr.drawString("y", 215, 405);
        gr.drawString("z", 206, 410);

        gr.drawString("a", 325, 295);
        gr.drawString("b", 345, 295);
        gr.drawString("4", 365, 315);
        gr.drawString("3", 365, 335);
        gr.drawString("2", 365, 355);
        gr.drawString("1", 365, 375);
        gr.drawString("w", 353, 395);
        gr.drawString("x", 344, 400);
        gr.drawString("y", 335, 405);
        gr.drawString("z", 326, 410);

        gr.drawString("a", 465, 295);
        gr.drawString("4", 485, 315);
        gr.drawString("3", 485, 335);
        gr.drawString("2", 485, 355);
        gr.drawString("1", 485, 375);
        gr.drawString("w", 473, 395);
        gr.drawString("x", 464, 400);
        gr.drawString("y", 455, 405);
        gr.drawString("z", 446, 410);

        gr.drawString("a", 26, 295);
        gr.drawString("b", 56, 295);
        gr.drawString("c", 86, 295);
        gr.drawString("d", 116, 295);
        gr.drawString("4", 7, 315);
        gr.drawString("3", 7, 345);
        gr.drawString("2", 7, 375);
        gr.drawString("1", 7, 405);
        gr.dispose();
        return image;
    }

    @Override
    public void stop() {
        status = 0;
        MiraiGamePlugin.INSTANCE.players_map.get(this.group.getId()).clear();
        MiraiGamePlugin.INSTANCE.games.remove(group.getId());
        group.sendMessage("游戏结束");
    }

    @Override
    public boolean allowGroup() {
        return true;
    }

    @Override
    public boolean isWaiting() {
        return status == 0;
    }

    @Override
    public int getMaxPlayer() {
        return 2;
    }

    @Override
    public void addPlayer(GamePlayer activePlayer) {
        if (player1 == null)
            player1 = activePlayer;
        else
            player2 = activePlayer;
    }

    @Override
    public void remove(GamePlayer activePlayer) {
        if (player1.equals(activePlayer))
            player1 = null;
        else
            player2 = null;
    }

}
