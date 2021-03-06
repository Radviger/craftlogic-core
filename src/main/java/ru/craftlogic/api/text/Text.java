package ru.craftlogic.api.text;

import net.minecraft.command.CommandException;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;

public abstract class Text<C extends ITextComponent, B extends Text<C, B>> {
    protected final List<ITextComponent> children = new ArrayList<>();
    protected Style style = new Style();

    public static TextString string() {
        return string("");
    }

    public static TextString string(String text) {
        return new TextString(text);
    }

    public static TextTranslation translation(CommandException exc) {
        TextTranslation text = translation(exc.getMessage());
        for (Object o : exc.getErrorObjects()) {
            if (o instanceof ITextComponent) {
                text.arg((ITextComponent)o);
            } else if (o instanceof Text){
                text.arg((Text)o);
            } else if (o instanceof String) {
                text.arg((String) o);
            } else if (o instanceof Number) {
                text.arg((Number)o);
            } else {
                text.arg(String.valueOf(o));
            }
        }
        return text;
    }

    public static TextTranslation translation(String format) {
        return new TextTranslation(format);
    }

    public static TextWrapped wrapped(ITextComponent component) {
        return new TextWrapped(component);
    }

    public static TextDate date(Date date, SimpleDateFormat format) {
        return new TextDate(date, format);
    }

    public B append(Text<?, ?> child) {
        this.children.add(child.build());
        return (B) this;
    }

    public B append(ITextComponent child) {
        this.children.add(child);
        return (B) this;
    }

    public B appendText(String arg) {
        return this.appendText(arg, null);
    }

    public B appendText(String arg, Consumer<TextString> decorator) {
        TextString text = Text.string(arg);
        if (decorator != null) {
            decorator.accept(text);
        }
        this.children.add(text.build());
        return (B) this;
    }

    public B appendTranslate(String format) {
        return this.appendTranslate(format, null);
    }

    public B appendTranslate(String format, Consumer<TextTranslation> decorator) {
        TextTranslation text = Text.translation(format);
        if (decorator != null) {
            decorator.accept(text);
        }
        this.children.add(text.build());
        return (B) this;
    }

    public B red() {
        this.style.setColor(TextFormatting.RED);
        return (B) this;
    }

    public B darkRed() {
        this.style.setColor(TextFormatting.DARK_RED);
        return (B) this;
    }

    public B green() {
        this.style.setColor(TextFormatting.GREEN);
        return (B) this;
    }

    public B darkGreen() {
        this.style.setColor(TextFormatting.DARK_GREEN);
        return (B) this;
    }

    public B blue() {
        this.style.setColor(TextFormatting.BLUE);
        return (B) this;
    }

    public B darkBlue() {
        this.style.setColor(TextFormatting.DARK_BLUE);
        return (B) this;
    }

    public B black() {
        this.style.setColor(TextFormatting.BLACK);
        return (B) this;
    }

    public B white() {
        this.style.setColor(TextFormatting.WHITE);
        return (B) this;
    }

    public B yellow() {
        this.style.setColor(TextFormatting.YELLOW);
        return (B) this;
    }

    public B gold() {
        this.style.setColor(TextFormatting.GOLD);
        return (B) this;
    }

    public B gray() {
        this.style.setColor(TextFormatting.GRAY);
        return (B) this;
    }

    public B darkGray() {
        this.style.setColor(TextFormatting.DARK_GRAY);
        return (B) this;
    }

    public B aqua() {
        this.style.setColor(TextFormatting.AQUA);
        return (B) this;
    }

    public B darkAqua() {
        this.style.setColor(TextFormatting.DARK_AQUA);
        return (B) this;
    }

    public B purple() {
        this.style.setColor(TextFormatting.LIGHT_PURPLE);
        return (B) this;
    }

    public B darkPurple() {
        this.style.setColor(TextFormatting.DARK_PURPLE);
        return (B) this;
    }

    public B color(TextFormatting color) {
        this.style.setColor(color);
        return (B) this;
    }

    public B italic() {
        this.style.setItalic(true);
        return (B) this;
    }

    public B underlined() {
        this.style.setUnderlined(true);
        return (B) this;
    }

    public B bold() {
        this.style.setBold(true);
        return (B) this;
    }

    public B strikethrough() {
        this.style.setStrikethrough(true);
        return (B) this;
    }

    public B obfuscated() {
        this.style.setObfuscated(true);
        return (B) this;
    }

    public B reset() {
        this.style = new Style();
        return (B) this;
    }

    public B suggestCommand(String command) {
        this.style.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, command));
        return (B) this;
    }

    public B runCommand(String command) {
        this.style.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command));
        return (B) this;
    }

    public B openUrl(String url) {
        this.style.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url));
        return (B) this;
    }

    public B openFile(String file) {
        this.style.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, file));
        return (B) this;
    }

    public B gotoPage(String command) {
        this.style.setClickEvent(new ClickEvent(ClickEvent.Action.CHANGE_PAGE, command));
        return (B) this;
    }

    public B hoverText(String arg) {
        return this.hoverText(arg, null);
    }

    public B hoverText(String arg, Consumer<TextString> decorator) {
        TextString text = Text.string(arg);
        if (decorator != null) {
            decorator.accept(text);
        }
        this.style.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, text.build()));
        return (B) this;
    }

    public B hoverTextTranslate(String arg) {
        return this.hoverTextTranslate(arg, null);
    }

    public B hoverTextTranslate(String arg, Consumer<TextTranslation> decorator) {
        TextTranslation text = Text.translation(arg);
        if (decorator != null) {
            decorator.accept(text);
        }
        this.style.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, text.build()));
        return (B) this;
    }

    public final C build() {
        C c = this.buildSub();
        for (ITextComponent child : this.children) {
            c.appendSibling(child);
        }
        c.setStyle(this.style);
        return c;
    }

    protected abstract C buildSub();
}
