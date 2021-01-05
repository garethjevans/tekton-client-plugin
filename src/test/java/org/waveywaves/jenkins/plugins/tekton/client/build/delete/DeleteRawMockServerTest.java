package org.waveywaves.jenkins.plugins.tekton.client.build.delete;

import io.fabric8.kubernetes.api.model.apiextensions.v1beta1.CustomResourceDefinition;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.fabric8.kubernetes.client.dsl.base.CustomResourceDefinitionContext;
import io.fabric8.kubernetes.client.server.mock.KubernetesServer;
import io.fabric8.tekton.pipeline.v1beta1.*;
import io.fabric8.tekton.resource.v1alpha1.PipelineResource;
import io.fabric8.tekton.resource.v1alpha1.PipelineResourceBuilder;
import io.fabric8.tekton.resource.v1alpha1.PipelineResourceList;
import org.junit.Rule;
import org.junit.Test;
import org.waveywaves.jenkins.plugins.tekton.client.TektonUtils;
import org.waveywaves.jenkins.plugins.tekton.client.build.create.CreateRaw;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;


public class DeleteRawMockServerTest {

    @Rule
    public KubernetesServer server = new KubernetesServer();

    @Test
    public void testTaskDelete() {
        // Given
        String testTaskYaml = "apiVersion: tekton.dev/v1beta1\n" +
                "kind: Task\n" +
                "metadata:\n" +
                "  name: testTask\n";
        String TEST_TASK = "testTask";

        KubernetesClient client = server.getClient();
        InputStream crdAsInputStream = getClass().getResourceAsStream("/task-crd.yaml");
        CustomResourceDefinition taskCrd = client.apiextensions().v1beta1().customResourceDefinitions().load(crdAsInputStream).get();
        MixedOperation<Task, TaskList, Resource<Task>> taskClient = client
                .customResources(CustomResourceDefinitionContext.fromCrd(taskCrd), Task.class, TaskList.class);

        // Mocked Responses
        TaskBuilder taskBuilder = new TaskBuilder()
                .withNewMetadata().withName(TEST_TASK).endMetadata();
        List<Task> tList = new ArrayList<>();
        Task testTask = taskBuilder.build();
        tList.add(testTask);
        TaskList taskList = new TaskList();
        taskList.setItems(tList);

        server.expect().post().withPath("/apis/tekton.dev/v1beta1/namespaces/test/tasks")
                .andReturn(HttpURLConnection.HTTP_CREATED, testTask).once();
        server.expect().get().withPath("/apis/tekton.dev/v1beta1/namespaces/test/tasks")
                .andReturn(HttpURLConnection.HTTP_OK, taskList).once();
        server.expect().delete().withPath("/apis/tekton.dev/v1beta1/namespaces/test/tasks/"+TEST_TASK)
                .andReturn(HttpURLConnection.HTTP_OK, testTask).once();
        server.expect().get().withPath("/apis/tekton.dev/v1beta1/namespaces/test/tasks")
                .andReturn(HttpURLConnection.HTTP_OK, new TaskList()).once();


        // When
        CreateRaw createRaw = new CreateRaw(CreateRaw.InputType.YAML.toString(), testTaskYaml);
        createRaw.setTektonClient(client);
        createRaw.setTaskClient(taskClient);
        createRaw.createTask(new ByteArrayInputStream(testTaskYaml.getBytes(StandardCharsets.UTF_8)));

        DeleteRaw.DeleteAllBlock deleteAllBlock = new DeleteRaw.DeleteAllBlock(TEST_TASK);

        DeleteRaw deleteRaw = new DeleteRaw(TektonUtils.TektonResourceType.task.toString(), deleteAllBlock);
        deleteRaw.setTektonClient(client);
        deleteRaw.setTaskClient(taskClient);
        Boolean isTaskDeleted = deleteRaw.deleteTask();

        // Then
        TaskList testTaskList = taskClient.list();
        assert isTaskDeleted.equals(true);
        assert testTaskList.getItems().size() == 0;
    }

    @Test
    public void testTaskDeleteAll() {
        // Given
        String testTask1Yaml = "apiVersion: tekton.dev/v1beta1\n" +
                "kind: Task\n" +
                "metadata:\n" +
                "  name: testTask1\n";
        String testTask2Yaml = "apiVersion: tekton.dev/v1beta1\n" +
                "kind: Task\n" +
                "metadata:\n" +
                "  name: testTask2\n";
        String TEST_TASK1 = "testTask1";
        String TEST_TASK2 = "testTask2";

        KubernetesClient client = server.getClient();
        InputStream crdAsInputStream = getClass().getResourceAsStream("/task-crd.yaml");
        CustomResourceDefinition taskCrd = client.apiextensions().v1beta1().customResourceDefinitions().load(crdAsInputStream).get();
        MixedOperation<Task, TaskList, Resource<Task>> taskClient = client
                .customResources(CustomResourceDefinitionContext.fromCrd(taskCrd), Task.class, TaskList.class);

        // Mocked Responses
        TaskBuilder taskBuilder = new TaskBuilder()
                .withNewMetadata().withName(TEST_TASK1).endMetadata();
        List<Task> tList = new ArrayList<>();
        Task testTask = taskBuilder.build();
        tList.add(testTask);
        TaskList taskList = new TaskList();
        taskList.setItems(tList);

        server.expect().post().withPath("/apis/tekton.dev/v1beta1/namespaces/test/tasks")
                .andReturn(HttpURLConnection.HTTP_CREATED, testTask).once();
        server.expect().get().withPath("/apis/tekton.dev/v1beta1/namespaces/test/tasks")
                .andReturn(HttpURLConnection.HTTP_OK, taskList).once();
        server.expect().delete().withPath("/apis/tekton.dev/v1beta1/namespaces/test/tasks/"+TEST_TASK1)
                .andReturn(HttpURLConnection.HTTP_OK, testTask).once();
        server.expect().get().withPath("/apis/tekton.dev/v1beta1/namespaces/test/tasks")
                .andReturn(HttpURLConnection.HTTP_OK, new TaskList()).once();

        // for Task 2
        taskBuilder = new TaskBuilder()
                .withNewMetadata().withName(TEST_TASK2).endMetadata();
        tList = new ArrayList<>();
        testTask = taskBuilder.build();
        tList.add(testTask);
        taskList = new TaskList();
        taskList.setItems(tList);

        server.expect().post().withPath("/apis/tekton.dev/v1beta1/namespaces/test/tasks")
                .andReturn(HttpURLConnection.HTTP_CREATED, testTask).once();
        server.expect().get().withPath("/apis/tekton.dev/v1beta1/namespaces/test/tasks")
                .andReturn(HttpURLConnection.HTTP_OK, taskList).once();
        server.expect().delete().withPath("/apis/tekton.dev/v1beta1/namespaces/test/tasks/"+TEST_TASK2)
                .andReturn(HttpURLConnection.HTTP_OK, testTask).once();
        server.expect().get().withPath("/apis/tekton.dev/v1beta1/namespaces/test/tasks")
                .andReturn(HttpURLConnection.HTTP_OK, new TaskList()).once();


        // When
        CreateRaw createRaw = new CreateRaw(CreateRaw.InputType.YAML.toString(), testTask1Yaml);
        createRaw.setTektonClient(client);
        createRaw.setTaskClient(taskClient);
        createRaw.createTask(new ByteArrayInputStream(testTask1Yaml.getBytes(StandardCharsets.UTF_8)));

        // Task 2
        createRaw = new CreateRaw(CreateRaw.InputType.YAML.toString(), testTask2Yaml);
        createRaw.setTektonClient(client);
        createRaw.setTaskClient(taskClient);
        createRaw.createTask(new ByteArrayInputStream(testTask2Yaml.getBytes(StandardCharsets.UTF_8)));

        DeleteRaw.DeleteAllBlock deleteAllBlock = new DeleteRaw.DeleteAllBlock(null);

        DeleteRaw deleteRaw = new DeleteRaw(TektonUtils.TektonResourceType.task.toString(), deleteAllBlock);
        deleteRaw.setTektonClient(client);
        deleteRaw.setTaskClient(taskClient);
        Boolean isTaskDeleted = deleteRaw.deleteTask();

        // Then
        TaskList testTaskList = taskClient.list();
        assert isTaskDeleted.equals(true);
        assert testTaskList.getItems().size() == 0;
    }

    @Test
    public void testTaskRunDelete() {
        // Given
        String testTaskRunYaml = "apiVersion: tekton.dev/v1beta1\n" +
                "kind: TaskRun\n" +
                "metadata:\n" +
                "  generateName: home-is-set-\n";
        String TEST_TASKRUN = "home-is-set-1234";

        KubernetesClient client = server.getClient();
        InputStream crdAsInputStream = getClass().getResourceAsStream("/taskrun-crd.yaml");
        CustomResourceDefinition taskRunCrd = client.apiextensions().v1beta1().customResourceDefinitions().load(crdAsInputStream).get();
        MixedOperation<TaskRun, TaskRunList, Resource<TaskRun>> taskRunClient = client
                .customResources(CustomResourceDefinitionContext.fromCrd(taskRunCrd), TaskRun.class, TaskRunList.class);

        // Mocked Responses
        TaskRunBuilder taskRunBuilder = new TaskRunBuilder()
                .withNewMetadata().withName(TEST_TASKRUN).endMetadata();
        List<TaskRun> trList = new ArrayList<>();
        TaskRun testTaskRun = taskRunBuilder.build();
        trList.add(testTaskRun);
        TaskRunList taskRunList = new TaskRunList();
        taskRunList.setItems(trList);

        server.expect().post().withPath("/apis/tekton.dev/v1beta1/namespaces/test/taskruns")
                .andReturn(HttpURLConnection.HTTP_CREATED, testTaskRun).once();
        server.expect().get().withPath("/apis/tekton.dev/v1beta1/namespaces/test/taskruns")
                .andReturn(HttpURLConnection.HTTP_OK, taskRunList).once();
        server.expect().delete().withPath("/apis/tekton.dev/v1beta1/namespaces/test/taskruns/"+TEST_TASKRUN)
                .andReturn(HttpURLConnection.HTTP_OK, testTaskRun).once();
        server.expect().get().withPath("/apis/tekton.dev/v1beta1/namespaces/test/taskruns")
                .andReturn(HttpURLConnection.HTTP_OK, new TaskList()).once();


        // When
        CreateRaw createRaw = new CreateRaw(CreateRaw.InputType.YAML.toString(), testTaskRunYaml){
            @Override
            public void streamTaskRunLogsToConsole(TaskRun taskRun) {
                return;
            }
        };
        createRaw.setTektonClient(client);
        createRaw.setTaskRunClient(taskRunClient);
        createRaw.createTaskRun(new ByteArrayInputStream(testTaskRunYaml.getBytes(StandardCharsets.UTF_8)));

        DeleteRaw.DeleteAllBlock deleteAllBlock = new DeleteRaw.DeleteAllBlock(TEST_TASKRUN);

        DeleteRaw deleteRaw = new DeleteRaw(TektonUtils.TektonResourceType.taskrun.toString(), deleteAllBlock);
        deleteRaw.setTektonClient(client);
        deleteRaw.setTaskRunClient(taskRunClient);
        Boolean isTaskRunDeleted = deleteRaw.deleteTaskRun();

        // Then
        TaskRunList testTaskRunList = taskRunClient.list();
        assert isTaskRunDeleted.equals(true);
        assert testTaskRunList.getItems().size() == 0;
    }

    @Test
    public void testTaskRunDeleteAll() {
        // Given
        String testTaskRun1Yaml = "apiVersion: tekton.dev/v1beta1\n" +
                "kind: TaskRun\n" +
                "metadata:\n" +
                "  name: home-is-set-1\n";
        String testTaskRun2Yaml = "apiVersion: tekton.dev/v1beta1\n" +
                "kind: TaskRun\n" +
                "metadata:\n" +
                "  name: home-is-set-2\n";
        String TEST_TASKRUN1 = "home-is-set-1";
        String TEST_TASKRUN2 = "home-is-set-2";

        KubernetesClient client = server.getClient();
        InputStream crdAsInputStream = getClass().getResourceAsStream("/taskrun-crd.yaml");
        CustomResourceDefinition taskRunCrd = client.apiextensions().v1beta1().customResourceDefinitions().load(crdAsInputStream).get();
        MixedOperation<TaskRun, TaskRunList, Resource<TaskRun>> taskRunClient = client
                .customResources(CustomResourceDefinitionContext.fromCrd(taskRunCrd), TaskRun.class, TaskRunList.class);

        // Mocked Responses
        TaskRunBuilder taskRunBuilder = new TaskRunBuilder()
                .withNewMetadata().withName(TEST_TASKRUN1).endMetadata();
        List<TaskRun> trList = new ArrayList<>();
        TaskRun testTaskRun = taskRunBuilder.build();
        trList.add(testTaskRun);
        TaskRunList taskRunList = new TaskRunList();
        taskRunList.setItems(trList);

        server.expect().post().withPath("/apis/tekton.dev/v1beta1/namespaces/test/taskruns")
                .andReturn(HttpURLConnection.HTTP_CREATED, testTaskRun).once();
        server.expect().get().withPath("/apis/tekton.dev/v1beta1/namespaces/test/taskruns")
                .andReturn(HttpURLConnection.HTTP_OK, taskRunList).once();
        server.expect().delete().withPath("/apis/tekton.dev/v1beta1/namespaces/test/taskruns/"+TEST_TASKRUN1)
                .andReturn(HttpURLConnection.HTTP_OK, testTaskRun).once();
        server.expect().get().withPath("/apis/tekton.dev/v1beta1/namespaces/test/taskruns")
                .andReturn(HttpURLConnection.HTTP_OK, new TaskList()).once();

        // TaskRun 2
        taskRunBuilder = new TaskRunBuilder()
                .withNewMetadata().withName(TEST_TASKRUN2).endMetadata();
        trList = new ArrayList<>();
        testTaskRun = taskRunBuilder.build();
        trList.add(testTaskRun);
        taskRunList = new TaskRunList();
        taskRunList.setItems(trList);

        server.expect().post().withPath("/apis/tekton.dev/v1beta1/namespaces/test/taskruns")
                .andReturn(HttpURLConnection.HTTP_CREATED, testTaskRun).once();
        server.expect().get().withPath("/apis/tekton.dev/v1beta1/namespaces/test/taskruns")
                .andReturn(HttpURLConnection.HTTP_OK, taskRunList).once();
        server.expect().delete().withPath("/apis/tekton.dev/v1beta1/namespaces/test/taskruns/"+TEST_TASKRUN2)
                .andReturn(HttpURLConnection.HTTP_OK, testTaskRun).once();
        server.expect().get().withPath("/apis/tekton.dev/v1beta1/namespaces/test/taskruns")
                .andReturn(HttpURLConnection.HTTP_OK, new TaskList()).once();


        // When
        CreateRaw createRaw = new CreateRaw(CreateRaw.InputType.YAML.toString(), testTaskRun1Yaml){
            @Override
            public void streamTaskRunLogsToConsole(TaskRun taskRun) {
                return;
            }
        };
        createRaw.setTektonClient(client);
        createRaw.setTaskRunClient(taskRunClient);
        createRaw.createTaskRun(new ByteArrayInputStream(testTaskRun1Yaml.getBytes(StandardCharsets.UTF_8)));

        // TaskRun 2
        createRaw = new CreateRaw(CreateRaw.InputType.YAML.toString(), testTaskRun2Yaml){
            @Override
            public void streamTaskRunLogsToConsole(TaskRun taskRun) {
                return;
            }
        };
        createRaw.setTektonClient(client);
        createRaw.setTaskRunClient(taskRunClient);
        createRaw.createTaskRun(new ByteArrayInputStream(testTaskRun2Yaml.getBytes(StandardCharsets.UTF_8)));

        DeleteRaw.DeleteAllBlock deleteAllBlock = new DeleteRaw.DeleteAllBlock(null);

        DeleteRaw deleteRaw = new DeleteRaw(TektonUtils.TektonResourceType.taskrun.toString(), deleteAllBlock);
        deleteRaw.setTektonClient(client);
        deleteRaw.setTaskRunClient(taskRunClient);
        Boolean isTaskRunDeleted = deleteRaw.deleteTaskRun();

        // Then
        TaskRunList testTaskRunList = taskRunClient.list();
        assert isTaskRunDeleted.equals(true);
        assert testTaskRunList.getItems().size() == 0;
    }

    @Test
    public void testPipelineDelete() {
        // Given
        String testPipelineYaml = "apiVersion: tekton.dev/v1beta1\n" +
                "kind: Pipeline\n" +
                "metadata:\n" +
                "  name: testPipeline\n";
        String TEST_PIPELINE = "testPipeline";

        KubernetesClient client = server.getClient();
        InputStream crdAsInputStream = getClass().getResourceAsStream("/pipeline-crd.yaml");
        CustomResourceDefinition pipelineCrd = client.apiextensions().v1beta1().customResourceDefinitions().load(crdAsInputStream).get();
        MixedOperation<Pipeline, PipelineList, Resource<Pipeline>> pipelineClient = client
                .customResources(CustomResourceDefinitionContext.fromCrd(pipelineCrd), Pipeline.class, PipelineList.class);

        // Mocked Responses
        PipelineBuilder pipelineBuilder = new PipelineBuilder()
                .withNewMetadata().withName(TEST_PIPELINE).endMetadata();
        List<Pipeline> pList = new ArrayList<>();
        Pipeline testPipeline = pipelineBuilder.build();
        pList.add(testPipeline);
        PipelineList pipelineList = new PipelineList();
        pipelineList.setItems(pList);

        server.expect().post().withPath("/apis/tekton.dev/v1beta1/namespaces/test/pipelines")
                .andReturn(HttpURLConnection.HTTP_CREATED, testPipeline).once();
        server.expect().get().withPath("/apis/tekton.dev/v1beta1/namespaces/test/pipelines")
                .andReturn(HttpURLConnection.HTTP_OK, pipelineList).once();
        server.expect().delete().withPath("/apis/tekton.dev/v1beta1/namespaces/test/pipelines/"+TEST_PIPELINE)
                .andReturn(HttpURLConnection.HTTP_OK, testPipeline).once();
        server.expect().get().withPath("/apis/tekton.dev/v1beta1/namespaces/test/pipelines")
                .andReturn(HttpURLConnection.HTTP_OK, new PipelineList()).once();


        // When
        CreateRaw createRaw = new CreateRaw(CreateRaw.InputType.YAML.toString(), testPipelineYaml);
        createRaw.setTektonClient(client);
        createRaw.setPipelineClient(pipelineClient);
        createRaw.createPipeline(new ByteArrayInputStream(testPipelineYaml.getBytes(StandardCharsets.UTF_8)));

        DeleteRaw.DeleteAllBlock deleteAllBlock = new DeleteRaw.DeleteAllBlock(TEST_PIPELINE);

        DeleteRaw deleteRaw = new DeleteRaw(TektonUtils.TektonResourceType.pipeline.toString(), deleteAllBlock);
        deleteRaw.setTektonClient(client);
        deleteRaw.setPipelineClient(pipelineClient);
        Boolean isPipelineDeleted = deleteRaw.deletePipeline();

        // Then
        PipelineList testPipelineList = pipelineClient.list();
        assert isPipelineDeleted.equals(true);
        assert testPipelineList.getItems().size() == 0;
    }

    @Test
    public void testPipelineDeleteAll() {
        // Given
        String testPipelineYaml1 = "apiVersion: tekton.dev/v1beta1\n" +
                "kind: Pipeline\n" +
                "metadata:\n" +
                "  name: testPipeline1\n";
        String TEST_PIPELINE1 = "testPipeline1";

        String testPipelineYaml2 = "apiVersion: tekton.dev/v1beta1\n" +
                "kind: Pipeline\n" +
                "metadata:\n" +
                "  name: testPipeline2\n";
        String TEST_PIPELINE2 = "testPipeline2";

        KubernetesClient client = server.getClient();
        InputStream crdAsInputStream = getClass().getResourceAsStream("/pipeline-crd.yaml");
        CustomResourceDefinition pipelineCrd = client.apiextensions().v1beta1().customResourceDefinitions().load(crdAsInputStream).get();
        MixedOperation<Pipeline, PipelineList, Resource<Pipeline>> pipelineClient = client
                .customResources(CustomResourceDefinitionContext.fromCrd(pipelineCrd), Pipeline.class, PipelineList.class);

        // Mocked Responses
        PipelineBuilder pipelineBuilder = new PipelineBuilder()
                .withNewMetadata().withName(TEST_PIPELINE1).endMetadata();
        List<Pipeline> pList = new ArrayList<>();
        Pipeline testPipeline = pipelineBuilder.build();
        pList.add(testPipeline);
        PipelineList pipelineList = new PipelineList();
        pipelineList.setItems(pList);

        server.expect().post().withPath("/apis/tekton.dev/v1beta1/namespaces/test/pipelines")
                .andReturn(HttpURLConnection.HTTP_CREATED, testPipeline).once();
        server.expect().get().withPath("/apis/tekton.dev/v1beta1/namespaces/test/pipelines")
                .andReturn(HttpURLConnection.HTTP_OK, pipelineList).once();
        server.expect().delete().withPath("/apis/tekton.dev/v1beta1/namespaces/test/pipelines/"+TEST_PIPELINE1)
                .andReturn(HttpURLConnection.HTTP_OK, testPipeline).once();
        server.expect().get().withPath("/apis/tekton.dev/v1beta1/namespaces/test/pipelines")
                .andReturn(HttpURLConnection.HTTP_OK, new PipelineList()).once();

        // Pipeline 2
        pipelineBuilder = new PipelineBuilder()
                .withNewMetadata().withName(TEST_PIPELINE2).endMetadata();
        pList = new ArrayList<>();
        testPipeline = pipelineBuilder.build();
        pList.add(testPipeline);
        pipelineList = new PipelineList();
        pipelineList.setItems(pList);

        server.expect().post().withPath("/apis/tekton.dev/v1beta1/namespaces/test/pipelines")
                .andReturn(HttpURLConnection.HTTP_CREATED, testPipeline).once();
        server.expect().get().withPath("/apis/tekton.dev/v1beta1/namespaces/test/pipelines")
                .andReturn(HttpURLConnection.HTTP_OK, pipelineList).once();
        server.expect().delete().withPath("/apis/tekton.dev/v1beta1/namespaces/test/pipelines/"+TEST_PIPELINE2)
                .andReturn(HttpURLConnection.HTTP_OK, testPipeline).once();
        server.expect().get().withPath("/apis/tekton.dev/v1beta1/namespaces/test/pipelines")
                .andReturn(HttpURLConnection.HTTP_OK, new PipelineList()).once();


        // When
        CreateRaw createRaw = new CreateRaw(CreateRaw.InputType.YAML.toString(), testPipelineYaml1);
        createRaw.setTektonClient(client);
        createRaw.setPipelineClient(pipelineClient);
        createRaw.createPipeline(new ByteArrayInputStream(testPipelineYaml1.getBytes(StandardCharsets.UTF_8)));

        //Pipeline 2
        createRaw = new CreateRaw(CreateRaw.InputType.YAML.toString(), testPipelineYaml2);
        createRaw.setTektonClient(client);
        createRaw.setPipelineClient(pipelineClient);
        createRaw.createPipeline(new ByteArrayInputStream(testPipelineYaml2.getBytes(StandardCharsets.UTF_8)));

        DeleteRaw.DeleteAllBlock deleteAllBlock = new DeleteRaw.DeleteAllBlock(null);

        DeleteRaw deleteRaw = new DeleteRaw(TektonUtils.TektonResourceType.pipeline.toString(), deleteAllBlock);
        deleteRaw.setTektonClient(client);
        deleteRaw.setPipelineClient(pipelineClient);
        Boolean isPipelineDeleted = deleteRaw.deletePipeline();

        // Then
        PipelineList testPipelineList = pipelineClient.list();
        assert isPipelineDeleted.equals(true);
        assert testPipelineList.getItems().size() == 0;
    }

    @Test
    public void testPipelineRunDelete() {
        // Given
        String testPipelineRunYaml = "apiVersion: tekton.dev/v1beta1\n" +
                "kind: PipelineRun\n" +
                "metadata:\n" +
                "  name: testPipelineRun\n";
        String TEST_PIPELINERUN = "testPipelineRun";

        KubernetesClient client = server.getClient();
        InputStream crdAsInputStream = getClass().getResourceAsStream("/pipelinerun-crd.yaml");
        CustomResourceDefinition pipelineRunCrd = client.apiextensions().v1beta1().customResourceDefinitions().load(crdAsInputStream).get();
        MixedOperation<PipelineRun, PipelineRunList, Resource<PipelineRun>> pipelineRunClient = client
                .customResources(CustomResourceDefinitionContext.fromCrd(pipelineRunCrd), PipelineRun.class, PipelineRunList.class);

        // Mocked Responses
        PipelineRunBuilder pipelineRunBuilder = new PipelineRunBuilder()
                .withNewMetadata().withName(TEST_PIPELINERUN).endMetadata();
        List<PipelineRun> pList = new ArrayList<>();
        PipelineRun testPipelineRun = pipelineRunBuilder.build();
        pList.add(testPipelineRun);
        PipelineRunList pipelineRunList = new PipelineRunList();
        pipelineRunList.setItems(pList);

        server.expect().post().withPath("/apis/tekton.dev/v1beta1/namespaces/test/pipelineruns")
                .andReturn(HttpURLConnection.HTTP_CREATED, testPipelineRun).once();
        server.expect().get().withPath("/apis/tekton.dev/v1beta1/namespaces/test/pipelineruns")
                .andReturn(HttpURLConnection.HTTP_OK, pipelineRunList).once();
        server.expect().delete().withPath("/apis/tekton.dev/v1beta1/namespaces/test/pipelineruns/"+TEST_PIPELINERUN)
                .andReturn(HttpURLConnection.HTTP_OK, testPipelineRun).once();
        server.expect().get().withPath("/apis/tekton.dev/v1beta1/namespaces/test/pipelineruns")
                .andReturn(HttpURLConnection.HTTP_OK, new PipelineRunList()).once();


        // When
        CreateRaw createRaw = new CreateRaw(CreateRaw.InputType.YAML.toString(), testPipelineRunYaml){
            @Override
            public void streamPipelineRunLogsToConsole(PipelineRun pipelineRun) {
                return;
            }
        };
        createRaw.setTektonClient(client);
        createRaw.setPipelineRunClient(pipelineRunClient);
        createRaw.createPipelineRun(new ByteArrayInputStream(testPipelineRunYaml.getBytes(StandardCharsets.UTF_8)));

        DeleteRaw.DeleteAllBlock deleteAllBlock = new DeleteRaw.DeleteAllBlock(TEST_PIPELINERUN);

        DeleteRaw deleteRaw = new DeleteRaw(TektonUtils.TektonResourceType.pipelinerun.toString(), deleteAllBlock);
        deleteRaw.setTektonClient(client);
        deleteRaw.setPipelineRunClient(pipelineRunClient);
        Boolean isPipelineRunDeleted = deleteRaw.deletePipelineRun();

        // Then
        PipelineRunList testPipelineRunList = pipelineRunClient.list();
        assert isPipelineRunDeleted.equals(true);
        assert testPipelineRunList.getItems().size() == 0;
    }

    @Test
    public void testPipelineRunDeleteAll() {
        // Given
        String testPipelineRun1Yaml = "apiVersion: tekton.dev/v1beta1\n" +
                "kind: PipelineRun\n" +
                "metadata:\n" +
                "  name: testPipelineRun1\n";
        String TEST_PIPELINERUN1 = "testPipelineRun1";

        String testPipelineRun2Yaml = "apiVersion: tekton.dev/v1beta1\n" +
                "kind: PipelineRun\n" +
                "metadata:\n" +
                "  name: testPipelineRun2\n";
        String TEST_PIPELINERUN2 = "testPipelineRun2";

        KubernetesClient client = server.getClient();
        InputStream crdAsInputStream = getClass().getResourceAsStream("/pipelinerun-crd.yaml");
        CustomResourceDefinition pipelineRunCrd = client.apiextensions().v1beta1().customResourceDefinitions().load(crdAsInputStream).get();
        MixedOperation<PipelineRun, PipelineRunList, Resource<PipelineRun>> pipelineRunClient = client
                .customResources(CustomResourceDefinitionContext.fromCrd(pipelineRunCrd), PipelineRun.class, PipelineRunList.class);

        // Mocked Responses
        PipelineRunBuilder pipelineRunBuilder = new PipelineRunBuilder()
                .withNewMetadata().withName(TEST_PIPELINERUN1).endMetadata();
        List<PipelineRun> pList = new ArrayList<>();
        PipelineRun testPipelineRun = pipelineRunBuilder.build();
        pList.add(testPipelineRun);
        PipelineRunList pipelineRunList = new PipelineRunList();
        pipelineRunList.setItems(pList);

        server.expect().post().withPath("/apis/tekton.dev/v1beta1/namespaces/test/pipelineruns")
                .andReturn(HttpURLConnection.HTTP_CREATED, testPipelineRun).once();
        server.expect().get().withPath("/apis/tekton.dev/v1beta1/namespaces/test/pipelineruns")
                .andReturn(HttpURLConnection.HTTP_OK, pipelineRunList).once();
        server.expect().delete().withPath("/apis/tekton.dev/v1beta1/namespaces/test/pipelineruns/"+TEST_PIPELINERUN1)
                .andReturn(HttpURLConnection.HTTP_OK, testPipelineRun).once();
        server.expect().get().withPath("/apis/tekton.dev/v1beta1/namespaces/test/pipelineruns")
                .andReturn(HttpURLConnection.HTTP_OK, new PipelineRunList()).once();

        // PipelineRun 2
        pipelineRunBuilder = new PipelineRunBuilder()
                .withNewMetadata().withName(TEST_PIPELINERUN2).endMetadata();
        pList = new ArrayList<>();
        testPipelineRun = pipelineRunBuilder.build();
        pList.add(testPipelineRun);
        pipelineRunList = new PipelineRunList();
        pipelineRunList.setItems(pList);

        server.expect().post().withPath("/apis/tekton.dev/v1beta1/namespaces/test/pipelineruns")
                .andReturn(HttpURLConnection.HTTP_CREATED, testPipelineRun).once();
        server.expect().get().withPath("/apis/tekton.dev/v1beta1/namespaces/test/pipelineruns")
                .andReturn(HttpURLConnection.HTTP_OK, pipelineRunList).once();
        server.expect().delete().withPath("/apis/tekton.dev/v1beta1/namespaces/test/pipelineruns/"+TEST_PIPELINERUN2)
                .andReturn(HttpURLConnection.HTTP_OK, testPipelineRun).once();
        server.expect().get().withPath("/apis/tekton.dev/v1beta1/namespaces/test/pipelineruns")
                .andReturn(HttpURLConnection.HTTP_OK, new PipelineRunList()).once();


        // When
        CreateRaw createRaw = new CreateRaw(CreateRaw.InputType.YAML.toString(), testPipelineRun1Yaml){
            @Override
            public void streamPipelineRunLogsToConsole(PipelineRun pipelineRun) {
                return;
            }
        };
        createRaw.setTektonClient(client);
        createRaw.setPipelineRunClient(pipelineRunClient);
        createRaw.createPipelineRun(new ByteArrayInputStream(testPipelineRun1Yaml.getBytes(StandardCharsets.UTF_8)));

        // PipelineRun 2
        createRaw = new CreateRaw(CreateRaw.InputType.YAML.toString(), testPipelineRun2Yaml){
            @Override
            public void streamPipelineRunLogsToConsole(PipelineRun pipelineRun) {
                return;
            }
        };
        createRaw.setTektonClient(client);
        createRaw.setPipelineRunClient(pipelineRunClient);
        createRaw.createPipelineRun(new ByteArrayInputStream(testPipelineRun2Yaml.getBytes(StandardCharsets.UTF_8)));

        DeleteRaw.DeleteAllBlock deleteAllBlock = new DeleteRaw.DeleteAllBlock(null);

        DeleteRaw deleteRaw = new DeleteRaw(TektonUtils.TektonResourceType.pipelinerun.toString(), deleteAllBlock);
        deleteRaw.setTektonClient(client);
        deleteRaw.setPipelineRunClient(pipelineRunClient);
        Boolean isPipelineRunDeleted = deleteRaw.deletePipelineRun();

        // Then
        PipelineRunList testPipelineRunList = pipelineRunClient.list();
        assert isPipelineRunDeleted.equals(true);
        assert testPipelineRunList.getItems().size() == 0;
    }

    @Test
    public void testPipelineResourceDelete() {
        // Given
        String testPipelineResourceYaml = "apiVersion: tekton.dev/v1alpha1\n" +
                "kind: PipelineResource\n" +
                "metadata:\n" +
                "  name: testPipelineResource\n";
        String TEST_PIPELINERESOURCE = "testPipelineResource";

        KubernetesClient client = server.getClient();
        InputStream crdAsInputStream = getClass().getResourceAsStream("/resource-crd.yaml");
        CustomResourceDefinition pipelineResourceCrd = client.apiextensions().v1beta1().customResourceDefinitions().load(crdAsInputStream).get();
        MixedOperation<PipelineResource, PipelineResourceList, Resource<PipelineResource>> pipelineResourceClient = client
                .customResources(CustomResourceDefinitionContext.fromCrd(pipelineResourceCrd), PipelineResource.class, PipelineResourceList.class);

        // Mocked Responses
        PipelineResourceBuilder pipelineResourceBuilder = new PipelineResourceBuilder()
                .withNewMetadata().withName(TEST_PIPELINERESOURCE).endMetadata();
        List<PipelineResource> pList = new ArrayList<>();
        PipelineResource testPipelineResource = pipelineResourceBuilder.build();
        pList.add(testPipelineResource);
        PipelineResourceList pipelineResourceList = new PipelineResourceList();
        pipelineResourceList.setItems(pList);

        server.expect().post().withPath("/apis/tekton.dev/v1alpha1/namespaces/test/pipelineresources")
                .andReturn(HttpURLConnection.HTTP_CREATED, testPipelineResource).once();
        server.expect().get().withPath("/apis/tekton.dev/v1alpha1/namespaces/test/pipelineresources")
                .andReturn(HttpURLConnection.HTTP_OK, pipelineResourceList).once();
        server.expect().delete().withPath("/apis/tekton.dev/v1alpha1/namespaces/test/pipelineresources/"+TEST_PIPELINERESOURCE)
                .andReturn(HttpURLConnection.HTTP_OK, testPipelineResource).once();
        server.expect().get().withPath("/apis/tekton.dev/v1alpha1/namespaces/test/pipelineresources")
                .andReturn(HttpURLConnection.HTTP_OK, new PipelineResourceList()).once();


        // When
        CreateRaw createRaw = new CreateRaw(CreateRaw.InputType.YAML.toString(), testPipelineResourceYaml);
        createRaw.setTektonClient(client);
        createRaw.setPipelineResourceClient(pipelineResourceClient);
        createRaw.createPipelineResource(new ByteArrayInputStream(testPipelineResourceYaml.getBytes(StandardCharsets.UTF_8)));

        DeleteRaw.DeleteAllBlock deleteAllBlock = new DeleteRaw.DeleteAllBlock(TEST_PIPELINERESOURCE);

        DeleteRaw deleteRaw = new DeleteRaw(TektonUtils.TektonResourceType.pipelineresource.toString(), deleteAllBlock);
        deleteRaw.setTektonClient(client);
        deleteRaw.setPipelineResourceClient(pipelineResourceClient);
        Boolean isPipelineDeleted = deleteRaw.deletePipelineResource();

        // Then
        PipelineResourceList testPipelineResourceList = pipelineResourceClient.list();
        assert isPipelineDeleted.equals(true);
        assert testPipelineResourceList.getItems().size() == 0;
    }

    @Test
    public void testPipelineResourceDeleteAll() {
        // Given
        String testPipelineResource1Yaml = "apiVersion: tekton.dev/v1alpha1\n" +
                "kind: PipelineResource\n" +
                "metadata:\n" +
                "  name: testPipelineResource1\n";
        String testPipelineResource2Yaml = "apiVersion: tekton.dev/v1alpha1\n" +
                "kind: PipelineResource\n" +
                "metadata:\n" +
                "  name: testPipelineResource2\n";
        String TEST_PIPELINERESOURCE1 = "testPipelineResource1";
        String TEST_PIPELINERESOURCE2 = "testPipelineResource2";

        KubernetesClient client = server.getClient();
        InputStream crdAsInputStream = getClass().getResourceAsStream("/resource-crd.yaml");
        CustomResourceDefinition pipelineResourceCrd = client.apiextensions().v1beta1().customResourceDefinitions().load(crdAsInputStream).get();
        MixedOperation<PipelineResource, PipelineResourceList, Resource<PipelineResource>> pipelineResourceClient = client
                .customResources(CustomResourceDefinitionContext.fromCrd(pipelineResourceCrd), PipelineResource.class, PipelineResourceList.class);

        // Mocked Responses
        PipelineResourceBuilder pipelineResourceBuilder = new PipelineResourceBuilder()
                .withNewMetadata().withName(TEST_PIPELINERESOURCE1).endMetadata();
        List<PipelineResource> pList = new ArrayList<>();
        PipelineResource testPipelineResource = pipelineResourceBuilder.build();
        pList.add(testPipelineResource);
        PipelineResourceList pipelineResourceList = new PipelineResourceList();
        pipelineResourceList.setItems(pList);

        server.expect().post().withPath("/apis/tekton.dev/v1alpha1/namespaces/test/pipelineresources")
                .andReturn(HttpURLConnection.HTTP_CREATED, testPipelineResource).once();
        server.expect().get().withPath("/apis/tekton.dev/v1alpha1/namespaces/test/pipelineresources")
                .andReturn(HttpURLConnection.HTTP_OK, pipelineResourceList).once();
        server.expect().delete().withPath("/apis/tekton.dev/v1alpha1/namespaces/test/pipelineresources/"+TEST_PIPELINERESOURCE1)
                .andReturn(HttpURLConnection.HTTP_OK, testPipelineResource).once();
        server.expect().get().withPath("/apis/tekton.dev/v1alpha1/namespaces/test/pipelineresources")
                .andReturn(HttpURLConnection.HTTP_OK, new PipelineResourceList()).once();

        // PipelineResource 2
        pipelineResourceBuilder = new PipelineResourceBuilder()
                .withNewMetadata().withName(TEST_PIPELINERESOURCE2).endMetadata();
        pList = new ArrayList<>();
        testPipelineResource = pipelineResourceBuilder.build();
        pList.add(testPipelineResource);
        pipelineResourceList = new PipelineResourceList();
        pipelineResourceList.setItems(pList);

        server.expect().post().withPath("/apis/tekton.dev/v1alpha1/namespaces/test/pipelineresources")
                .andReturn(HttpURLConnection.HTTP_CREATED, testPipelineResource).once();
        server.expect().get().withPath("/apis/tekton.dev/v1alpha1/namespaces/test/pipelineresources")
                .andReturn(HttpURLConnection.HTTP_OK, pipelineResourceList).once();
        server.expect().delete().withPath("/apis/tekton.dev/v1alpha1/namespaces/test/pipelineresources/"+TEST_PIPELINERESOURCE2)
                .andReturn(HttpURLConnection.HTTP_OK, testPipelineResource).once();
        server.expect().get().withPath("/apis/tekton.dev/v1alpha1/namespaces/test/pipelineresources")
                .andReturn(HttpURLConnection.HTTP_OK, new PipelineResourceList()).once();

        // When
        CreateRaw createRaw = new CreateRaw(CreateRaw.InputType.YAML.toString(), testPipelineResource1Yaml);
        createRaw.setTektonClient(client);
        createRaw.setPipelineResourceClient(pipelineResourceClient);
        createRaw.createPipelineResource(new ByteArrayInputStream(testPipelineResource1Yaml.getBytes(StandardCharsets.UTF_8)));

        //PipelineResource 2
        createRaw = new CreateRaw(CreateRaw.InputType.YAML.toString(), testPipelineResource2Yaml);
        createRaw.setTektonClient(client);
        createRaw.setPipelineResourceClient(pipelineResourceClient);
        createRaw.createPipelineResource(new ByteArrayInputStream(testPipelineResource2Yaml.getBytes(StandardCharsets.UTF_8)));


        DeleteRaw.DeleteAllBlock deleteAllBlock = new DeleteRaw.DeleteAllBlock(null);

        DeleteRaw deleteRaw = new DeleteRaw(TektonUtils.TektonResourceType.pipelineresource.toString(), deleteAllBlock);
        deleteRaw.setTektonClient(client);
        deleteRaw.setPipelineResourceClient(pipelineResourceClient);
        Boolean isPipelineDeleted = deleteRaw.deletePipelineResource();

        // Then
        PipelineResourceList testPipelineResourceList = pipelineResourceClient.list();
        assert isPipelineDeleted.equals(true);
        assert testPipelineResourceList.getItems().size() == 0;
    }
}