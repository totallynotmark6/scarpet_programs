__config()-> {
    'strict': true,
    'commands' -> {
        '' -> 'cmd_get_hammer_mode',
        'toggle' -> 'cmd_toggle_hammer_mode',
        'set <width> <height>' -> 'cmd_set_hammer_mode',
        'remove' -> 'cmd_remove_hammer_data'
    },
    'arguments' -> {
        'width' -> {
            'type' -> 'int',
            'min' -> 1,
            'max' -> 13,
            'suggest' -> [1, 3, 5, 7]
        },
        'height' -> {
            'type' -> 'int',
            'min' -> 1,
            'max' -> 13,
            'suggest' -> [1, 2, 3, 5, 7]
        }
    },
};

cmd_get_hammer_mode()-> (
    hammer_data = get_hammer_data(player());
    print(player(), hammer_data);
);

cmd_toggle_hammer_mode()-> (
    held_item = query(player(), 'holds', 'mainhand');
    hammer_data = get_hammer_data(player());
    update_hammer_data(!hammer_data:0, hammer_data:1, hammer_data:2);
    display_title(player(), 'actionbar', 'Hammer: ' + !hammer_data:0);
);

cmd_set_hammer_mode(width, height)-> (
    held_item = query(player(), 'holds', 'mainhand');
    update_hammer_data(true, width, height);
);

cmd_remove_hammer_data()-> (
    player = player();
    slot = query(player, 'selected_slot');
    item = inventory_get(player, slot);

    nbt = parse_nbt(item:2);
    delete(nbt, 'tnm6Hammer.enabled');
    delete(nbt, 'tnm6Hammer.width');
    delete(nbt, 'tnm6Hammer.height');

    inventory_set(player, slot, item:1, item:0, encode_nbt(nbt));
);

global_breaklist = null;

__on_player_breaks_block(player, block)-> (
    hammer_data = get_hammer_data(player);
    if (hammer_data:0 && global_breaklist, (
        // enabled!
        // do the thing!
        for(global_breaklist, (
            harvest(player, _);
            slot = query(player, 'selected_slot');
            item = inventory_get(player, slot);
            if (item:0 == null, (
                // broken!!!
                return('cancel');
            ));
        ));
        return('cancel');
    ));
);

__on_player_clicks_block(player, block, face)-> (
    hammer_data = get_hammer_data(player);
    if (hammer_data:0, (
        // enabled! figure out what's gonna be broken.
        global_breaklist = get_aoe(player, pos(block), face);
    ), (
        global_breaklist = null;
    ));
);

update_hammer_data(enabled, width, height)-> (
    player = player();
    slot = query(player, 'selected_slot');
    item = inventory_get(player, slot);

    nbt = parse_nbt(item:2);
    put(nbt, 'tnm6Hammer.enabled', enabled);
    put(nbt, 'tnm6Hammer.width', width);
    put(nbt, 'tnm6Hammer.height', height);

    inventory_set(player, slot, item:1, item:0, encode_nbt(nbt));
);

get_hammer_data(player)-> (
    slot = query(player, 'selected_slot');
    item = inventory_get(player, slot);

    nbt = parse_nbt(item:2);
    enabled = bool(get(nbt, 'tnm6Hammer.enabled'));
    width = get(nbt, 'tnm6Hammer.width');
    height = get(nbt, 'tnm6Hammer.height');

    return([enabled, width, height]);
);

get_aoe(player, position, face) -> (
    hammer_data = get_hammer_data(player);
    width = hammer_data:1 / 2 - 1;
    height = hammer_data:2 - 2;
    l(x, y, z) = position;
    if (face == 'up' || face == 'down', (
        // horizontal
        height = hammer_data:2 / 2 - 1;
        list = l();
        volume(
            l(x - round(width), y, z - round(height)),
            l(x + round(width), y, z + round(height)),
            (
                put(list, null, _);
            )
        );
        list = filter(list, harvestable(_));
        return(list);
    ), face == 'north' || face == 'south', (
        list = l();
        volume(
            l(x - round(width), y - 1, z),
            l(x + round(width), y + height, z),
            (
                put(list, null, _);
            )
        );
        return(list);
    ), (
        // east/west
        list = l();
        volume(
            l(x, y - 1, z - round(width)),
            l(x, y + height, z + round(width)),
            (
                put(list, null, _);
            )
        );
        return(list);
    ));
);

harvestable(block) -> !(air(block) || (block == 'lava') || (block == 'water'));