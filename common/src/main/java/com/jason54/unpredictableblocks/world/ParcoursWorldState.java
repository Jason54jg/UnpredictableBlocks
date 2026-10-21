package com.jason54.unpredictableblocks.world;

import com.jason54.unpredictableblocks.UnpredictableBlocks;
import com.mojang.serialization.Codec;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

/** État session parcours (portes coop, vote, seed du layout). */
public final class ParcoursWorldState {
    private ParcoursWorldState() {}

    public static Session get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(Session.TYPE);
    }

    public static final class Session extends SavedData {
        private static final Codec<Session> CODEC = CompoundTag.CODEC.xmap(Session::fromTag, Session::toTag);

        public static final SavedDataType<Session> TYPE = new SavedDataType<>(
                Identifier.fromNamespaceAndPath(UnpredictableBlocks.MOD_ID, "parcours_session"),
                Session::new,
                CODEC,
                DataFixTypes.SAVED_DATA_SCOREBOARD
        );

        private boolean sessionOpen;
        private int coopSectionBreaks;
        private int coopSectionIndex;
        private int gatesMask;
        private long layoutSeed;

        private Session() {}

        public boolean sessionOpen() {
            return sessionOpen;
        }

        public void setSessionOpen(boolean sessionOpen) {
            this.sessionOpen = sessionOpen;
            setDirty();
        }

        public long layoutSeed() {
            return layoutSeed;
        }

        public void setLayoutSeed(long layoutSeed) {
            this.layoutSeed = layoutSeed;
            setDirty();
        }

        public boolean gateOpen(int gateIndex) {
            return (gatesMask & (1 << gateIndex)) != 0;
        }

        public void openGate(int gateIndex) {
            gatesMask |= (1 << gateIndex);
            setDirty();
        }

        public void recordCoopBreak(int requiredPlayers) {
            coopSectionBreaks++;
            if (coopSectionBreaks >= requiredPlayers) {
                openGate(coopSectionIndex);
                coopSectionBreaks = 0;
                coopSectionIndex++;
            }
            setDirty();
        }

        public void resetCoopProgress() {
            coopSectionBreaks = 0;
            coopSectionIndex = 0;
            gatesMask = 0;
            setDirty();
        }

        private static Session fromTag(CompoundTag tag) {
            Session s = new Session();
            s.sessionOpen = tag.getBooleanOr("SessionOpen", false);
            s.coopSectionBreaks = tag.getIntOr("CoopBreaks", 0);
            s.coopSectionIndex = tag.getIntOr("CoopSection", 0);
            s.gatesMask = tag.getIntOr("Gates", 0);
            s.layoutSeed = tag.getLongOr("LayoutSeed", 0L);
            return s;
        }

        private CompoundTag toTag() {
            CompoundTag tag = new CompoundTag();
            tag.putBoolean("SessionOpen", sessionOpen);
            tag.putInt("CoopBreaks", coopSectionBreaks);
            tag.putInt("CoopSection", coopSectionIndex);
            tag.putInt("Gates", gatesMask);
            tag.putLong("LayoutSeed", layoutSeed);
            return tag;
        }
    }
}
