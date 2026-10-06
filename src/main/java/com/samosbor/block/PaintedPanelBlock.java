package com.samosbor.block;

import eu.pb4.polymer.core.api.block.SimplePolymerBlock;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Blocks;

public class PaintedPanelBlock extends SimplePolymerBlock {
    public PaintedPanelBlock() {
        super(AbstractBlock.Settings.copy(Blocks.STONE_BRICKS).strength(2.0f, 6.0f), Blocks.CYAN_TERRACOTTA);
    }
}
