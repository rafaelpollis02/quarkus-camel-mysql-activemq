package br.com.hospital.mediation;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.GET;

@ApplicationScoped
public class PatientMediation extends RouteBuilder {

    /**
     author: rpollis
     **/

    @Override
    public void configure() throws Exception {
        from("timer:myTimerHospital?period=10000")
                .setBody(constant("select * from patient where state = 'N'"))
                .end()
                .to("jdbc:mysql-hospital-ds")
                .split(body())
                .transform(simple("insert into patient (id, name, state) values (${body['id']}, '${body['name']}', 'Integracao')"))
                .to("jdbc:mysql-aws-ds")
                .to("direct:update");


        from("direct:update")
                .setBody(constant("update patient set state = 'S' where state = 'N'"))
                .end()
                .to("jdbc:mysql-hospital-ds")
                .to("activemq:pollis-mq");
    }
}
