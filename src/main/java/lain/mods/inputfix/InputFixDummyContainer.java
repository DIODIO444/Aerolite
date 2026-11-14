package lain.mods.inputfix;

import java.util.Arrays;
import net.minecraftforge.fml.common.DummyModContainer;
import net.minecraftforge.fml.common.LoadController;
import com.google.common.eventbus.EventBus;
import net.minecraftforge.fml.common.ModMetadata;

public class InputFixDummyContainer extends DummyModContainer
{

    public InputFixDummyContainer() {
        super(new ModMetadata());
        ModMetadata meta = getMetadata();
        meta.modId = "liquidbounce_inputfix";
        meta.name = "InputFix (IME)";
        meta.version = "1.8.9";
        meta.authorList = Arrays.asList("lain");
        meta.description = "Chinese / Japanese / Korean input method support for Minecraft.";
        meta.credits = "";
        meta.url = "https://github.com/lain-sys/InputFix";
    }

    @Override
    public boolean registerBus(EventBus bus, LoadController controller)
    {
        return true;
    }

}