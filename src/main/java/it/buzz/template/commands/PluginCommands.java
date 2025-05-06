package it.buzz.template.commands;

import it.buzz.template.TemplatePlugin;
import lombok.RequiredArgsConstructor;
import revxrsal.commands.annotation.Command;

@Command("")
@RequiredArgsConstructor
public class PluginCommands {

    private final TemplatePlugin plugin;

}
