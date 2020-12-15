package pokecube.legends.blocks.customblocks;

import net.minecraft.util.IStringSerializable;

public enum YveltalEggPart implements IStringSerializable
{
  TOP("top"),
  BOTTOM("bottom");

  private final String name;

  YveltalEggPart(final String name)
  {
    this.name = name;
  }

  @Override
  public String getString()
  {
    return this.name;
  }
}