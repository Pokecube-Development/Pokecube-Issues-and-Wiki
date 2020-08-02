package pokecube.legends.blocks.customblocks;

import net.minecraft.util.IStringSerializable;

public enum YveltalEggPart implements IStringSerializable
{
  TOP("top"),
  BOTTOM("bottom");

  private final String name;

  YveltalEggPart(String name)
  {
    this.name = name;
  }

  @Override
  public String getName()
  {
    return this.name;
  }
}