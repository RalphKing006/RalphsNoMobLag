package com.zenya.nomoblag.util;

import com.zenya.nomoblag.NoMobLag;
import org.bukkit.entity.Entity;
import org.bukkit.metadata.FixedMetadataValue;

public class MetaUtils {

  public static boolean hasMeta(Entity entity, String meta) {
    return entity.hasMetadata(meta);
  }

  public static void setMeta(Entity entity, String meta, Object value) {
    entity.setMetadata(meta, new FixedMetadataValue(NoMobLag.getInstance(), value));
  }

  public static void clearMeta(Entity entity, String meta) {
    if (hasMeta(entity, meta)) {
      entity.removeMetadata(meta, NoMobLag.getInstance());
    }
  }

  public static String getMetaValue(Entity entity, String meta) {
    if (!(hasMeta(entity, meta)) || entity.getMetadata(meta).isEmpty()) {
      return "";
    }
    return entity.getMetadata(meta).get(0).asString();
  }
}
