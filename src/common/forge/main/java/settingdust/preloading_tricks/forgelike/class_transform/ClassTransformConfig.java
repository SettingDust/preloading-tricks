package settingdust.preloading_tricks.forgelike.class_transform;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public record ClassTransformConfig(
    @SerializedName("package")
    String packageName,
    @SerializedName("transformers")
    List<String> transformers
) {}
