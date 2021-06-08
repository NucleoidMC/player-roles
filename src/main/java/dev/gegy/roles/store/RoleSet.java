package dev.gegy.roles.store;

import com.google.common.collect.AbstractIterator;
import dev.gegy.roles.config.PlayerRolesConfig;
import dev.gegy.roles.Role;
import it.unimi.dsi.fastutil.objects.ObjectRBTreeSet;

import java.util.AbstractSet;
import java.util.Iterator;

public final class RoleSet extends AbstractSet<Role> {
    private final ObjectRBTreeSet<String> ids = new ObjectRBTreeSet<>((leftId, rightId) -> {
        var config = PlayerRolesConfig.get();
        var left = config.get(leftId);
        var right = config.get(rightId);
        if (left == null || right == null) return 0;
        return left.compareTo(right);
    });

    @Override
    public void clear() {
        this.ids.clear();
    }

    @Override
    public boolean add(Role role) {
        return this.ids.add(role.getName());
    }

    @Override
    public boolean remove(Object obj) {
        if (obj instanceof Role) {
            return this.ids.remove(((Role) obj).getName());
        }
        return false;
    }

    @Override
    public boolean contains(Object obj) {
        if (obj instanceof Role) {
            return this.ids.contains(((Role) obj).getName());
        }
        return false;
    }

    public boolean containsId(String id) {
        return this.ids.contains(id);
    }

    @Override
    public Iterator<Role> iterator() {
        var config = PlayerRolesConfig.get();
        var idIterator = this.ids.iterator();

        return new AbstractIterator<Role>() {
            @Override
            protected Role computeNext() {
                while (idIterator.hasNext()) {
                    var id = idIterator.next();
                    var role = config.get(id);
                    if (role != null) {
                        return role;
                    }
                }
                return this.endOfData();
            }
        };
    }

    @Override
    public int size() {
        return this.ids.size();
    }
}
