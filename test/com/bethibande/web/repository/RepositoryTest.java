package com.bethibande.web.repository;

import com.bethibande.web.JWebClient;
import org.junit.jupiter.api.Test;

import java.net.MalformedURLException;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.*;

public class RepositoryTest {

    @Test
    public void testVirtualMethods() throws MalformedURLException {
        final JWebClient client = new JWebClient()
                .withBaseUrl("http://127.0.0.1:4455");

        final TestRepo repo = client.withRepository(TestRepo.class);

        assertEquals(client, repo.getOwner());
        assertEquals(new URL("http://127.0.0.1:4455"), repo.getBaseUrl());

        repo.setBaseUrl("http://127.0.0.1:4456");
        assertEquals(new URL("http://127.0.0.1:4456"), repo.getBaseUrl());
    }

}
