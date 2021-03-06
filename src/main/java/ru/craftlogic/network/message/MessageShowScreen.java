package ru.craftlogic.network.message;

import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import ru.craftlogic.api.block.holders.ScreenHolder;
import ru.craftlogic.api.inventory.InventoryFieldHolder;
import ru.craftlogic.api.inventory.InventoryHolder;
import ru.craftlogic.api.network.AdvancedBuffer;
import ru.craftlogic.api.network.AdvancedMessage;
import ru.craftlogic.api.world.Locatable;
import ru.craftlogic.api.world.Location;
import ru.craftlogic.util.ReflectiveUsage;

import java.util.ArrayList;
import java.util.List;

public class MessageShowScreen extends AdvancedMessage {
    private HolderType type;
    private Location location;
    private int windowId;
    private int extraData;
    private List<Integer> fields = new ArrayList<>();
    private int entityId;

    @Deprecated
    @ReflectiveUsage
    public MessageShowScreen() {}

    public MessageShowScreen(ScreenHolder screenHolder, int windowId, int extraData) {
        this.windowId = windowId;
        this.extraData = extraData;
        if (screenHolder instanceof Entity) {
            entityId = ((Entity) screenHolder).getEntityId();
            type = HolderType.ENTITY;
        } else if (screenHolder instanceof TileEntity && screenHolder instanceof Locatable) {
            location = ((Locatable) screenHolder).getLocation();
            type = HolderType.TILE;
        }
        if (screenHolder instanceof InventoryHolder) {
            InventoryFieldHolder fieldHolder = ((InventoryHolder) screenHolder).getFieldHolder();
            if (fieldHolder != null) {
                for (int i = 0; i < fieldHolder.getInvFieldCount(); i++) {
                    int v = fieldHolder.getInvFieldValue(i);
                    fields.add(v);
                }
            }
        }
    }

    public HolderType getType() {
        return type;
    }

    public Location getLocation() {
        return location;
    }

    public int getWindowId() {
        return windowId;
    }

    public int getEntityId() {
        return entityId;
    }

    public int getExtraData() {
        return extraData;
    }

    public List<Integer> getFields() {
        return fields;
    }

    @Override
    protected void read(AdvancedBuffer buf) {
        type = buf.readEnumValue(HolderType.class);
        windowId = buf.readInt();
        int fieldCount = buf.readInt();
        if (fieldCount > 0) {
            for (int i = 0; i < fieldCount; i++) {
                fields.add((int) buf.readShort());
            }
        }
        switch (type) {
            case TILE:
                location = buf.readBlockLocation();
                break;
            case ENTITY:
                entityId = buf.readVarInt();
                break;
        }
        extraData = buf.readInt();
    }

    @Override
    protected void write(AdvancedBuffer buf) {
        buf.writeEnumValue(type);
        buf.writeInt(windowId);
        int fieldCount = fields.size();
        buf.writeInt(fieldCount);
        if (fieldCount > 0) {
            for (int field : fields) {
                buf.writeShort(field);
            }
        }
        switch (type) {
            case TILE:
                buf.writeBlockLocation(location);
                break;
            case ENTITY:
                buf.writeVarInt(entityId);
                break;
        }
        buf.writeInt(extraData);
    }

    public enum HolderType {
        TILE,
        ENTITY
    }
}
