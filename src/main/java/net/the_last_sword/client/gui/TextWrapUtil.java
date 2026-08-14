package net.the_last_sword.client.gui;

import net.minecraft.client.gui.Font;

import java.util.ArrayList;
import java.util.List;

//文本自动换行工具（支持中英文混合和颜色代码，英文按单词边界换行）
public class TextWrapUtil {

    public static List<String> wrapText(Font font, String text, int maxWidth) {
        List<String> lines = new ArrayList<>();
        if (text == null || text.isEmpty()) {
            return lines;
        }

        StringBuilder currentLine = new StringBuilder();
        StringBuilder currentWord = new StringBuilder(); //当前正在构建的单词
        String activeColor = ""; //当前激活的颜色代码
        boolean inWord = false; //是否在英文单词中

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);

            //显式换行符
            if (c == '\n') {
                if (currentWord.length() > 0) {
                    currentLine.append(currentWord);
                    currentWord = new StringBuilder();
                }
                if (currentLine.length() > 0) {
                    lines.add(currentLine.toString());
                }
                currentLine = new StringBuilder();
                if (!activeColor.isEmpty()) {
                    currentLine.append(activeColor);
                }
                inWord = false;
                continue;
            }

            //检测颜色代码（§ + 一个字符）
            if (c == '§' && i + 1 < text.length()) {
                char colorCode = text.charAt(i + 1);
                //添加颜色代码到当前单词
                currentWord.append(c).append(colorCode);
                //更新当前激活的颜色
                if (colorCode == 'r') {
                    activeColor = ""; //重置颜色
                } else {
                    activeColor = "§" + colorCode;
                }
                i++; //跳过颜色代码的第二个字符
                continue;
            }

            //判断是否是英文字符或数字
            boolean isEnglishChar = (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || (c >= '0' && c <= '9');

            if (isEnglishChar) {
                //英文字符加入当前单词
                currentWord.append(c);
                inWord = true;
            } else if (c == ' ') {
                //空格：结束当前单词，尝试加入当前行
                currentWord.append(c);
                String testLine = currentLine.toString() + currentWord.toString();

                if (font.width(testLine) <= maxWidth) {
                    //能放下，加入当前行
                    currentLine.append(currentWord);
                } else {
                    //放不下，换行
                    if (currentLine.length() > 0) {
                        lines.add(currentLine.toString().trim()); //去除行尾空格
                        currentLine = new StringBuilder();
                        if (!activeColor.isEmpty()) {
                            currentLine.append(activeColor);
                        }
                    }
                    //将单词加入新行（去除前导空格）
                    currentLine.append(currentWord.toString().trim());
                }
                currentWord = new StringBuilder();
                inWord = false;
            } else {
                //其他字符（中文、标点等）：先结束当前单词，然后逐字符处理
                if (currentWord.length() > 0) {
                    //先处理缓存的单词
                    String testLine = currentLine.toString() + currentWord.toString();
                    if (font.width(testLine) <= maxWidth) {
                        currentLine.append(currentWord);
                    } else {
                        if (currentLine.length() > 0) {
                            lines.add(currentLine.toString().trim());
                            currentLine = new StringBuilder();
                            if (!activeColor.isEmpty()) {
                                currentLine.append(activeColor);
                            }
                        }
                        currentLine.append(currentWord.toString().trim());
                    }
                    currentWord = new StringBuilder();
                }

                //处理当前字符
                String testLine = currentLine.toString() + c;
                if (font.width(testLine) <= maxWidth) {
                    currentLine.append(c);
                } else {
                    //中文字符可以直接换行
                    if (currentLine.length() > 0) {
                        lines.add(currentLine.toString());
                        currentLine = new StringBuilder();
                        if (!activeColor.isEmpty()) {
                            currentLine.append(activeColor);
                        }
                    }
                    currentLine.append(c);
                }
                inWord = false;
            }
        }

        //处理最后的单词
        if (currentWord.length() > 0) {
            String testLine = currentLine.toString() + currentWord.toString();
            if (font.width(testLine) <= maxWidth) {
                currentLine.append(currentWord);
            } else {
                if (currentLine.length() > 0) {
                    lines.add(currentLine.toString().trim());
                    currentLine = new StringBuilder();
                    if (!activeColor.isEmpty()) {
                        currentLine.append(activeColor);
                    }
                }
                currentLine.append(currentWord.toString().trim());
            }
        }

        //添加最后一行
        if (currentLine.length() > 0) {
            lines.add(currentLine.toString());
        }

        return lines;
    }
}
