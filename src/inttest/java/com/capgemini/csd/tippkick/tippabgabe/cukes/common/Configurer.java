package com.capgemini.csd.tippkick.tippabgabe.cukes.common;

import com.capgemini.csd.tippkick.tippabgabe.cukes.steps.to.TippTestTO;
import cucumber.api.TypeRegistry;
import cucumber.api.TypeRegistryConfigurer;
import io.cucumber.datatable.DataTableType;

import java.util.Locale;

public class Configurer implements TypeRegistryConfigurer {

    @Override
    public Locale locale() {
        return Locale.ENGLISH;
    }

    @Override
    public void configureTypeRegistry(TypeRegistry typeRegistry) {
        typeRegistry.defineDataTableType(DataTableType.entry(TippTestTO.class));
    }

}
