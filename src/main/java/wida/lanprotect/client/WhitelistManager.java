package wida.lanprotect.client;

import java.util.HashSet;
import java.util.Set;

public class WhitelistManager {

    protected WhitelistManager() {  }

    private Set<String> whitelisted = new HashSet<>();
    private Set<String> banned = new HashSet<>();

    public void whitelist(String name) {
        whitelisted.add(name);
    }

    public void ban(String name) {
        whitelisted.remove(name);
        banned.add(name);
    }

    public void clear() {
        whitelisted.clear();
        banned.clear();
    }

    public boolean isWhitelisted(String name) {
        return whitelisted.contains(name);
    }

    public boolean isBanned(String name) {
        return banned.contains(name);
    }

    public boolean shouldAsk(String name) {
        return !isWhitelisted(name) && !isBanned(name);
    }
}
