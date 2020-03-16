package pokecube.legends.blocks;

import net.minecraft.util.IStringSerializable;

public enum UltraSpacePortalPart implements IStringSerializable
{
  TOP("top"),
  TOP_LEFT("top_left"),
  TOP_RIGHT("top_right"),
  MIDDLE("middle"),
  MIDDLE_LEFT("middle_left"),
  MIDDLE_RIGHT("middle_right"),
  BOTTOM("bottom"),
  BOTTOM_LEFT("bottom_left"),
  BOTTOM_RIGHT("bottom_right");

  private final String name;

  UltraSpacePortalPart(String name)
  {
    this.name = name;
  }

  @Override
  public String getName()
  {
    return this.name;
  }
}