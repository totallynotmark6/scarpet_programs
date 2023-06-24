__config()-> {
    'strict': true,
    'commands' -> {
        '' -> 'cmd_get_autotorch',
        'toggle' -> 'cmd_toggle_autotorch',
        'set <level>' -> 'cmd_set_autotorch_level',
    },
    'arguments' -> {
        'level' -> {
            'type' -> 'int',
            'min' -> 1,
            'max' -> 15,
            'suggest' -> [1, 8]
        }
    },
};

global_autotorch_enabled = false;
global_autotorch_level = 1;

cmd_get_autotorch()-> (
    print(player(), 'Autotorch: ' + global_autotorch_enabled);
    print(player(), 'Level: ' + global_autotorch_level);
);

cmd_toggle_autotorch()-> (
    global_autotorch_enabled = !global_autotorch_enabled;
    print(player(), 'Autotorch: ' + global_autotorch_enabled);
);

cmd_set_autotorch_level(level)-> (
    global_autotorch_level = level;
    print(player(), 'Level: ' + global_autotorch_level);
);


__on_tick()->(
    if (global_autotorch_enabled, (
        dimension = query(player(), 'dimension');
        in_dimension(dimension, (
            player_pos = pos(player());
            beneath = pos_offset(player_pos, 'down');
            // display_title(player(), 'actionbar', effective_light(player_pos));
            if (solid(beneath) && effective_light(player_pos) <= global_autotorch_level, (
                if (inventory_remove(player(), 'torch', 1) == 1, (
                    place_item('torch', player_pos);
                    display_title(player(), 'actionbar', 'Placed torch!');
                ));
            ));
        ));
    ));
)