package com.example.owner.spreadsheet.utils;

import android.graphics.Canvas;
import android.graphics.Paint;

public class DrawText {

    public static final int ALIGNMENT_RIGHT = 1;
    public static final int ALIGNMENT_LEFT = 2;
    public static final int ALIGNMENT_CENTER = 3;

    // sheet 에 보여지는 cell 을 그려주는 메소드
    // edit 중인지에 따라서 다르다. edit 중일 경우는 ellipsis 는 false, edit 중이 아닐 경우는 true.
    public void drawText(String text, int alignment, float x, float y, float width, float height, Paint paint, Canvas canvas, boolean ellipsis) {
        float textWidth;
        // edit 중일 경우
        if (ellipsis) {
            Paint.FontMetrics fontMetrics = paint.getFontMetrics();
            float textHeight = (int) ((fontMetrics.top * -1) - fontMetrics.bottom);

            textWidth = 0;
            int textStartPoint = 0;
            float textTempWidth = 0;
            int i;
            for (i = textStartPoint; i < text.length(); i++) {
                textTempWidth += paint.measureText(text, i, i + 1);
                if (textTempWidth > width) {
                    break;
                } else {
                    textWidth = textTempWidth;
                }
            }

            float fontX = (width - textWidth) / 2;
            float fontY = ((height + textHeight) / 2);


            switch (alignment) {
                // 오른쪽 정렬
                case ALIGNMENT_RIGHT:
                    canvas.drawText(text.substring(0, i), x + (2 * fontX), y + fontY, paint);
                    break;
                // 왼쪽 정렬
                case ALIGNMENT_LEFT:
                    canvas.drawText(text.substring(0, i), x, y + fontY, paint);
                    break;
                // 가운데 정렬
                case ALIGNMENT_CENTER:
                    break;

                default:
                    canvas.drawText(text, x, y + fontY, paint);

            }

        }// edit 중이 아닐 경우
        else {
            Paint.FontMetrics fontMetrics = paint.getFontMetrics();
            float textHeight = (fontMetrics.top * -1) - fontMetrics.bottom;
            float fontY = ((height + textHeight) / 2);

            canvas.drawText(text, x, y + fontY, paint);
        }
    }
}
