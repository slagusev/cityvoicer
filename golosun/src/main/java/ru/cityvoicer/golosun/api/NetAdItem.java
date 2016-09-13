package ru.cityvoicer.golosun.api;

import ru.cityvoicer.golosun.Utils;
import ru.cityvoicer.golosun.model.EAdFlagMedia;

public class NetAdItem {
    public String id;
    public String image;
    public String id_places;
    public String logo;
    public String name;
    public String description;
    public int priority;
    public int flag_media;
    public int date;

    public boolean update(NetAdItem other) {
        boolean updated = false;
        if (!Utils.equals(image, other.image)) {
            image = other.image;
            updated = true;
        }
        if (!Utils.equals(id_places, other.id_places)) {
            id_places = other.id_places;
            updated = true;
        }
        if (!Utils.equals(logo, other.logo)) {
            logo = other.logo;
            updated = true;
        }
        if (!Utils.equals(name, other.name)) {
            name = other.name;
            updated = true;
        }
        if (!Utils.equals(description, other.description)) {
            description = other.description;
            updated = true;
        }
        if (!Utils.equals(priority, other.priority)) {
            priority = other.priority;
            updated = true;
        }
        if (!Utils.equals(flag_media, other.flag_media)) {
            flag_media = other.flag_media;
            updated = true;
        }
        if (!Utils.equals(date, other.date)) {
            date = other.date;
            updated = true;
        }
        return updated;
    }

    public boolean isVideo() {
        return flag_media == EAdFlagMedia.VIDEO.get();
    }

    public static final String EXPAND_ALL_PARAMS = "image,id_places,logo,name,description,priority,flag_media,date";
}

