package com.samosbor.block;

import eu.pb4.polymer.core.api.block.SimplePolymerBlock;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Blocks;

public class P46PanelBlock extends SimplePolymerBlock {
    public P46PanelBlock() {
        super(AbstractBlock.Settings.copy(Blocks.SMOOTH_STONE).strength(2.5f, 6.0f), Blocks.SMOOTH_STONE);
    }
}
