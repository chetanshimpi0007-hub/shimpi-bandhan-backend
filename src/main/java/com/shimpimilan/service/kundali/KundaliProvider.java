package com.shimpimilan.service.kundali;

import com.shimpimilan.model.Kundali;

import java.util.Map;

public interface KundaliProvider {
    Map<String, Object> calculateMatch(Kundali k1, Kundali k2);
}
