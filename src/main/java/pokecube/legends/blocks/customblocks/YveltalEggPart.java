package pokecube.legends.blocks.customblocks;

import net.minecraft.util.StringRepresentable;

public enum YveltalEggPart implements StringRepresentable
{
  TOP("top"),
  BOTTOM("bottom");

  private final String name;

  YveltalEggPart(final String name)
  {
    this.name = name;
  }

  @Override
  public String getSerializedName()
  {
    return this.name;
  }
}