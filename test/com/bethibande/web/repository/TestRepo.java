package com.bethibande.web.repository;

import com.bethibande.web.annotations.URI;
import com.bethibande.web.types.RequestMethod;

import java.util.HashMap;

public interface TestRepo extends Repository {

    @URI(value = "/localCache", methods = RequestMethod.GET)
    HashMap<String, Object> cache();
}
