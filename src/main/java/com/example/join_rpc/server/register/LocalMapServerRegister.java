package com.example.join_rpc.server.register;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LocalMapServerRegister extends DefaultServerRegister{
    @Override
    public void remove() {
        log.info("LocalMapServerRegister remove");
    }
}
