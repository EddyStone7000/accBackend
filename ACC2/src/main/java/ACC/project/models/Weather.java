package ACC.project.models;

public class Weather {
    private String main;
    private String icon; // Neues Feld für Icon-ID

    public String getMain() {
        return main;
    }

    public void setMain(String main) {
        this.main = main;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }
}