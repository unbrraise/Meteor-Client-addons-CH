package nekiplay.meteorplus.features.modules;

import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.StringSetting;
import meteordevelopment.meteorclient.systems.modules.Module;
import nekiplay.meteorplus.MeteorPlus;

public class ChatGPT extends Module {
	public ChatGPT() {
		super(MeteorPlus.CATEGORY, "Chat GPT", "在我的世界里使用Chat GPT");
	}

	private final SettingGroup sgGeneral = settings.getDefaultGroup();

	public enum Service {
		NovaAI,
		NagaAI;

		@Override
		public String toString() {
			return super.toString().replace('_', ' ');
		}
	}

	public final Setting<Service> service = sgGeneral.add(new EnumSetting.Builder<Service>()
		.name("服务")
		.description("访问Chat GPT的服务")
		.defaultValue(Service.NagaAI)
		.build()
	);

	public final Setting<String> token_novaai = sgGeneral.add(new StringSetting.Builder()
		.name("NovaAI令牌")
		.description("从NovaAI获取的令牌")
		.defaultValue("")
		.visible(() -> service.get() == Service.NovaAI)
		.build()
	);

	public final Setting<String> token_nagaai = sgGeneral.add(new StringSetting.Builder()
		.name("NagaAI令牌")
		.description("从NagaAI获取的令牌")
		.defaultValue("")
		.visible(() -> service.get() == Service.NagaAI)
		.build()
	);
}
