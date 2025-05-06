package it.buzz.template.manager;

import it.buzz.template.TemplatePlugin;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class AbstractManager {

    @Getter protected final TemplatePlugin plugin;

    public abstract void start();
    public abstract void stop();

}
