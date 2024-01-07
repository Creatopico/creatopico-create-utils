package ru.littleligr.creatopico.create.utils;

import eu.midnightdust.lib.config.MidnightConfig;
import net.fabricmc.api.ModInitializer;


import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.littleligr.creatopico.create.utils.redis.RedisManager;

import java.util.HashSet;
import java.util.Set;

public class CreatopicoCreateUtils implements ModInitializer {
	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final String MODID = "creatopico_create_utils";
    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

	public static Set<Identifier> dimensionsWorksIn = new HashSet<>();
	public static Set<Block> breakBlocks = new HashSet<>();
	public static RedisManager REDIS_MANAGER;

	@Override
	public void onInitialize() {
		MidnightConfig.init(MODID, ConfigModel.class);
		REDIS_MANAGER = new RedisManager(ConfigModel.REDIS_IP, ConfigModel.PORT);

		dimensionsWorksIn.add(new Identifier("minecraft", "blank"));

		breakBlocks.add(Blocks.AIR);

		breakBlocks.add(Blocks.GRASS);
		breakBlocks.add(Blocks.FERN);
		breakBlocks.add(Blocks.LARGE_FERN);

		breakBlocks.add(Blocks.WATER);

		breakBlocks.add(Blocks.OAK_LEAVES);
		breakBlocks.add(Blocks.ACACIA_LEAVES);
		breakBlocks.add(Blocks.BIRCH_LEAVES);
		breakBlocks.add(Blocks.CHERRY_LEAVES);
		breakBlocks.add(Blocks.AZALEA_LEAVES);
		breakBlocks.add(Blocks.DARK_OAK_LEAVES);
		breakBlocks.add(Blocks.FLOWERING_AZALEA_LEAVES);
		breakBlocks.add(Blocks.MANGROVE_LEAVES);
		breakBlocks.add(Blocks.SPRUCE_LEAVES);

		breakBlocks.add(Blocks.VINE);
		breakBlocks.add(Blocks.CAVE_VINES);
		breakBlocks.add(Blocks.TWISTING_VINES);
		breakBlocks.add(Blocks.WEEPING_VINES);
		breakBlocks.add(Blocks.CAVE_VINES_PLANT);
		breakBlocks.add(Blocks.TWISTING_VINES_PLANT);
		breakBlocks.add(Blocks.WEEPING_VINES_PLANT);

		breakBlocks.add(Blocks.CORNFLOWER);
		breakBlocks.add(Blocks.SUNFLOWER);
		breakBlocks.add(Blocks.TORCHFLOWER);
	}
}