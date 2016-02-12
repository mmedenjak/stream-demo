package com.ingemark.stream.controller;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ConfigController {
    @Autowired
    private SessionFactory sf;

//    @RequestMapping(value = "/config", method = GET)
//    public Stream<ConfigDto> good() {
//        final Query q = sf.getCurrentSession().createQuery("select new " + ConfigDto.class.getName() + "(id, key,value) from Config");
//        return resultStream(ConfigDto.class, q);
//    }

//    @RequestMapping(value = "/config", method = GET)
//    public Stream<Config> bad() {
//        return resultStream(Config.class, sf.getCurrentSession().createQuery("from Config"));
//    }

//    @RequestMapping(value = "/config", method = GET)
//    public Stream<Config> stillBad() {
//        return resultStream(Config.class, sf.openStatelessSession().createQuery("from Config"));
//    }

//    @RequestMapping(value = "/config", method = GET)
//    public void stillBad() {
//        sf.getCurrentSession().doWork(connection -> {
//            resultStream(Config.class, sf.openStatelessSession(connection).createQuery("from Config")); // do stuff - writer.write, outstream.write?
//        });
//    }

//    @RequestMapping(value = "/config", method = GET)
//    @Transactional(readOnly = true)
//    public Stream<ConfigDto> notOK() {
//        final Query q = sf.getCurrentSession().createQuery("select new " + ConfigDto.class.getName() + "(id, key,value) from Config");
//        return resultStream(ConfigDto.class, q);
//    }

    // inside transaction? NO - use good, YES - use good or stillBad
}
