package me.fullpage.manticlib.wrappers;

import de.exlll.configlib.annotation.ConfigurationElement;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
@Getter
@ConfigurationElement
public class ForwardingData {

    private String target = "";
    private List<String> aliases = new ArrayList<>();


}
