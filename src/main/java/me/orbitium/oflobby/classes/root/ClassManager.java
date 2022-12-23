package me.orbitium.oflobby.classes.root;

import me.orbitium.oflobby.classes.attacker.*;
import me.orbitium.oflobby.classes.defender.*;
import me.orbitium.oflobby.classes.hero.*;
import org.bukkit.entity.Player;
import org.checkerframework.checker.units.qual.A;

import java.util.*;

public class ClassManager {

    public static Map<ClassType, List<OFClass>> classes = new HashMap<>();
    public static Alchemist alchemist = new Alchemist();
    public static Baker baker = new Baker();
    public static Blacksmith blacksmith = new Blacksmith();
    public static Builder builder = new Builder();
    public static Lumberjack lumberjack = new Lumberjack();

    public static Zombie zombie = new Zombie();
    public static Skeleton skeleton = new Skeleton();
    public static Spider spider = new Spider();
    public static Creeper creeper = new Creeper();
    public static Silverfish silverfish = new Silverfish();

    public static Zachattck zachattck = new Zachattck();
    public static VoidWalker voidWalker = new VoidWalker();
    public static Tiger tiger = new Tiger();
    public static Sorcerer sorcerer = new Sorcerer();
    public static Paladin paladin = new Paladin();
    public static Koko koko = new Koko();
    public static BroodMother broodMother = new BroodMother();


    public static void loadClasses() {
        List<OFClass> defenders = new ArrayList<>();
        defenders.add(alchemist);
        defenders.add(baker);
        defenders.add(blacksmith);
        defenders.add(builder);
        defenders.add(lumberjack);

        List<OFClass> attackers = new ArrayList<>();
        attackers.add(zombie);
        attackers.add(skeleton);
        attackers.add(spider);
        attackers.add(creeper);
        attackers.add(silverfish);

        List<OFClass> heroes = new ArrayList<>();
        heroes.add(zachattck);
        heroes.add(voidWalker);
        heroes.add(tiger);
        heroes.add(sorcerer);
        heroes.add(paladin);
        heroes.add(koko);
        heroes.add(broodMother);

        classes.put(ClassType.Defender, defenders);
        classes.put(ClassType.Zombies, attackers);
        classes.put(ClassType.Heroes, heroes);
    }

    public static OFClass getByName(String name) {
        for (Map.Entry<ClassType, List<OFClass>> classTypeListEntry : classes.entrySet()) {
            for (OFClass ofClass : classTypeListEntry.getValue()) {
                if (ofClass.name.toLowerCase(Locale.US).equals(name))
                    return ofClass;
            }
        }
        return null;
    }

    public static OFClass getClassFromPlayer(Player player) {
        for (Map.Entry<ClassType, List<OFClass>> classTypeListEntry : classes.entrySet()) {
            for (OFClass ofClass : classTypeListEntry.getValue()) {
                if (ofClass.registeredPlayers.contains(player))
                    return ofClass;
            }
        }
        return null;
    }
}
