package org.goodev.droidddle;

public interface ThemeActivity {
    int getMyTheme();

    void setMyTheme(int id);

    /**
     * same as Activity setTheme
     */
    void setTheme(int resid);

    void recreate();
}
