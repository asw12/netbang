package netbang.gui;

import java.awt.Color;
import java.awt.Polygon;

public class InfoText extends Clickable {
    String text;
    Color color;
    Color currentcolor;
    int alpha;

    public InfoText(Polygon p, String text, Color color) {
        super(p, null);
        animation |= FADEIN;
        this.text = text;
        this.color = color;
        currentcolor = new Color(color.getRGB() & ~(255 << 24));
    }

    /**
     * Fades the InfoText.
     *
     * @param amount
     *            1 for fade in, -1 for fade out
     * @see netbang.gui.Clickable#fade(int)
     */
    public void fade() {
        System.out.println(color.getAlpha());
        alpha = color.getAlpha() + 26
                * ((animation & FADEIN) != 0 ? 1 : 0);
        if (alpha > 255)
            alpha = 255;
        if (alpha < 0)
            alpha = 0;
        currentcolor = new Color((color.getRGB() & ~(255 << 24)) | alpha << 24);
        if ((animation & FADEIN)!=0 && alpha == 255)
            animation &= ~FADEIN;
        else if ((animation & FADEOUT)!=0 && alpha == 0)
            animation &= ~FADEOUT;
    }

}
