package me.orbitium.ofdvz.classes.root;

public enum ClassType {
    Defender("defenders"),
    Zombies("zombies"),
    Heroes("heroes");

    String path;

    ClassType(String path) {
        this.path = path;
    }

}
