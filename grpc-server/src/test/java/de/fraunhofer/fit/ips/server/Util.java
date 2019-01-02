package de.fraunhofer.fit.ips.server;

import de.fraunhofer.fit.ips.proto.structure.Function;
import de.fraunhofer.fit.ips.proto.structure.Level;
import de.fraunhofer.fit.ips.proto.structure.MultilingualRichtext;
import de.fraunhofer.fit.ips.proto.structure.Particle;
import de.fraunhofer.fit.ips.proto.structure.Project;
import de.fraunhofer.fit.ips.proto.structure.QName;
import de.fraunhofer.fit.ips.proto.structure.Request;
import de.fraunhofer.fit.ips.proto.structure.Response;
import de.fraunhofer.fit.ips.proto.structure.Service;
import de.fraunhofer.fit.ips.proto.structure.Text;
import lombok.experimental.UtilityClass;

import java.util.UUID;
import java.util.function.Consumer;

/**
 * @author Fabian Ohler <fabian.ohler1@rwth-aachen.de>
 */
@UtilityClass
public class Util {
    public static String newIdentifier() {
        return UUID.randomUUID().toString();
    }

    public static Project.ProjectChild newPLevel(final Consumer<Level.Builder> consumer) {
        return newPLevel(false, consumer);
    }

    public static Project.ProjectChild newPLevel(final boolean suppressNumbering,
                                                 final Consumer<Level.Builder> consumer) {
        final Level.Builder builder = Level.newBuilder().setIdentifier(newIdentifier()).setSuppressNumbering(suppressNumbering);
        consumer.accept(builder);
        return Project.ProjectChild.newBuilder().setLevel(builder).build();
    }

    public static Service.ServiceChild newSText(final MultilingualRichtext richtext) {
        return Service.ServiceChild.newBuilder().setText(Text.newBuilder().setRtContent(richtext)).build();
    }

    public static Level.LevelChild newLText(final MultilingualRichtext richtext) {
        return Level.LevelChild.newBuilder().setText(Text.newBuilder().setRtContent(richtext)).build();
    }

    public static Project.ProjectChild newService(final String name, final Consumer<Service.Builder> consumer) {
        final Service.Builder builder = Service.newBuilder().setIdentifier(newIdentifier()).setName(name);
        consumer.accept(builder);
        return Project.ProjectChild.newBuilder().setService(builder).build();
    }

    public static Service.ServiceChild newFunction(final String name, final Consumer<Function.Builder> consumer) {
        final Function.Builder builder = Function.newBuilder().setIdentifier(newIdentifier()).setName(name);
        consumer.accept(builder);
        return Service.ServiceChild.newBuilder().setFunction(builder).build();
    }

    public static Function.FunctionChild newRequest(final Consumer<Request.Builder> consumer) {
        final Request.Builder builder = Request.newBuilder().setIdentifier(newIdentifier());
        consumer.accept(builder);
        return Function.FunctionChild.newBuilder().setRequest(builder).build();
    }

    public static Function.FunctionChild newResponse(final Consumer<Response.Builder> consumer) {
        final Response.Builder builder = Response.newBuilder().setIdentifier(newIdentifier());
        consumer.accept(builder);
        return Function.FunctionChild.newBuilder().setResponse(builder).build();
    }

    public static Function.FunctionChild newAssertion(final Consumer<Function.Assertion.Builder> consumer) {
        final Function.Assertion.Builder builder = Function.Assertion.newBuilder();
        consumer.accept(builder);
        return Function.FunctionChild.newBuilder().setAssertion(builder).build();
    }

    public static Level.LevelChild newLParticle(final String namespaceUri, final String ncName) {
        return Level.LevelChild.newBuilder().setParticle(Particle.newBuilder().setQName(newQName(namespaceUri, ncName)).build()).build();
    }

    public static Request.RequestResponseChild newRParticle(final String namespaceUri, final String ncName) {
        return Request.RequestResponseChild.newBuilder().setParticle(Particle.newBuilder().setQName(Util.newQName(namespaceUri, ncName)).build()).build();
    }

    public static QName newQName(final String namespaceUri, final String ncName) {
        return QName.newBuilder().setNamespaceUri(namespaceUri).setNcName(ncName).build();
    }
}
