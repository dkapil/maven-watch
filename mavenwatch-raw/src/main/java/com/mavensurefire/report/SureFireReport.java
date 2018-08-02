package com.mavensurefire.report;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Element;

import com.mavensurefire.pom.Build;
import com.mavensurefire.pom.Build.Plugins;
import com.mavensurefire.pom.Model;
import com.mavensurefire.pom.Parent;
import com.mavensurefire.pom.Plugin;
import com.mavensurefire.pom.Plugin.Configuration;
import com.mavensurefire.report.model.Project;
import com.mavensurefire.report.model.SurefireConfiguration;

public class SureFireReport {

    public static void main(String[] args) throws IOException, JAXBException {
        String root = "E:\\Study\\Baeldung\\repo\\tutorials";
        createReport(root);
    }

    static void createReport(String root) throws IOException, JAXBException {

        List<Path> pomXmlPaths = Files.walk(Paths.get(root))
            .filter(p -> p.toString()
                .endsWith("pom.xml"))
            .collect(Collectors.toList());

        JAXBContext jaxbContext = JAXBContext.newInstance(Model.class);
        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

        Map<String, Project> projectsMap = populateProjectsMap(pomXmlPaths, jaxbUnmarshaller);

        List<String> rows = new ArrayList<>();

        for (Entry<String, Project> projectEntry : projectsMap.entrySet()) {
            Project project = projectEntry.getValue();

            List<String> columns = new ArrayList<>();
            columns.add(project.getId());

            List<String> parents = new ArrayList<>();

            SurefireConfiguration surefireConfiguration = getProjectConfigurationRecursively(project, parents, projectsMap);

            if (parents.size() > 1) {
                columns.add("YES");
            } else {
                columns.add("NO");
            }

            columns.add(StringUtils.join(parents, " << "));
            if (surefireConfiguration != null) {
                columns.add(StringUtils.join(surefireConfiguration.getInclusions(), ";"));
                columns.add(StringUtils.join(surefireConfiguration.getExclusions(), ";"));
            }

            rows.add(StringUtils.join(columns, ","));
        }
        // rows.forEach(System.out::println);
    }

    private static SurefireConfiguration getProjectConfigurationRecursively(Project project, List<String> parents, Map<String, Project> projectsMap) {
        if (project == null)
            return null;

        parents.add(project.getId());

        if (project.getParentId() == null)
            return project.getSurefireConfiguration();

        if (project.getSurefireConfiguration() != null) {
            return project.getSurefireConfiguration();
        } else {
            return getProjectConfigurationRecursively(projectsMap.get(project.getParentId()), parents, projectsMap);
        }
    }

    private static Map<String, Project> populateProjectsMap(List<Path> pomXmlPaths, Unmarshaller jaxbUnmarshaller) {

        Map<String, Project> projectsMap = new LinkedHashMap<>();

        for (Path pomXmlPath : pomXmlPaths) {

            List<String> rowElements = new ArrayList<>();
            try {
                Object unmarshalledObject = jaxbUnmarshaller.unmarshal(pomXmlPath.toFile());
                if (unmarshalledObject instanceof JAXBElement<?> && ((JAXBElement<?>) unmarshalledObject).getValue() instanceof Model) {
                    Object object = ((JAXBElement<?>) unmarshalledObject).getValue();
                    Model pomModel = (Model) object;

                    rowElements.add(pomModel.getArtifactId());

                    Project project = new Project();
                    project.setId(pomModel.getArtifactId());
                    projectsMap.put(pomModel.getArtifactId(), project);

                    Parent parent = pomModel.getParent();

                    if (parent != null) {
                        project.setParentId(parent.getArtifactId());
                    }

                    Build build = pomModel.getBuild();

                    String folderName = pomXmlPath.getParent()
                        .getFileName()
                        .toString();

                    String shortpath = StringUtils.substringAfter(pomXmlPath.toString(), "repo\\");
                    if (!folderName.equalsIgnoreCase(pomModel.getArtifactId())) {
                        System.out.println(shortpath + "," + folderName + "," + pomModel.getArtifactId() + "," + pomModel.getName());
                    }
                    if (build != null && build.getPlugins() != null) {
                        Plugins plugins = build.getPlugins();

                        Optional<Plugin> surefirepluginoptional = plugins.getPlugin()
                            .stream()
                            .filter(p -> p.getArtifactId()
                                .equalsIgnoreCase("maven-surefire-plugin"))
                            .findAny();

                        if (surefirepluginoptional.isPresent()) {
                            rowElements.add("Plugin Present");

                            SurefireConfiguration surefireConfiguration = createConfiguration(surefirepluginoptional.get());
                            project.setSurefireConfiguration(surefireConfiguration);

                        }
                    }
                }
            } catch (JAXBException e) {
                //
            }
        }
        return projectsMap;
    }

    private static SurefireConfiguration createConfiguration(Plugin plugin) {

        SurefireConfiguration surefireConfiguration = null;

        if (plugin.getConfiguration() != null) {
            surefireConfiguration = new SurefireConfiguration();
            Configuration configuration = plugin.getConfiguration();

            Optional<Element> includesOptional = configuration.getAny()
                .stream()
                .filter(e -> e.getNodeName()
                    .equalsIgnoreCase("includes"))
                .findAny();

            Optional<Element> excludesOptional = configuration.getAny()
                .stream()
                .filter(e -> e.getNodeName()
                    .equalsIgnoreCase("excludes"))
                .findAny();

            List<String> inclusions = new ArrayList<>();

            List<String> exclusions = new ArrayList<>();

            if (includesOptional.isPresent() || excludesOptional.isPresent()) {

                if (includesOptional.isPresent()) {
                    IntStream.range(0, includesOptional.get()
                        .getChildNodes()
                        .getLength())
                        .forEach(e -> inclusions.add(includesOptional.get()
                            .getChildNodes()
                            .item(e)
                            .getTextContent()
                            .trim()));

                    surefireConfiguration.setInclusions(inclusions);
                }
                if (excludesOptional.isPresent()) {
                    IntStream.range(0, excludesOptional.get()
                        .getChildNodes()
                        .getLength())
                        .forEach(e -> exclusions.add(excludesOptional.get()
                            .getChildNodes()
                            .item(e)
                            .getTextContent()
                            .trim()));

                    surefireConfiguration.setExclusions(exclusions);
                }
            }
        }
        return surefireConfiguration;
    }
}
