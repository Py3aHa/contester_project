package com.project.SptingSecurity.controllers;

import com.project.SptingSecurity.Repositories.CategoriesRepositories;
import com.project.SptingSecurity.Repositories.TasksRepositories;
import com.project.SptingSecurity.Repositories.UserRepositories;
import com.project.SptingSecurity.entities.Lessons;
import com.project.SptingSecurity.entities.TaskCategories;
import com.project.SptingSecurity.entities.Tasks;
import com.project.SptingSecurity.entities.Users;
import org.hibernate.dialect.FirebirdDialect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RootUriTemplateHandler;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

//сделать поиск по слову
//

@Controller
@RequestMapping(path = "/task")
public class TasksController {

    @Autowired
    UserRepositories userRepositories;

    @Autowired
    TasksRepositories tasksRepositories;

    @Autowired
    CategoriesRepositories categoriesRepositories;

    public Users getUserData(){
        Users userData = null;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(!(authentication instanceof AnonymousAuthenticationToken)){
            User secUser = (User)authentication.getPrincipal();
            userData = userRepositories.findByEmail(secUser.getUsername());
        }
        return userData;
    }

    @GetMapping(path = "tasks")
    public String tasksPage(Model model, @RequestParam(name = "page", defaultValue = "1") int page){
        int size = tasksRepositories.countAllBy();

        int tabSize = (size+4)/10;

        if(tabSize < 1){
            tabSize = 1;
        }

        Pageable pageable = PageRequest.of(page-1,12);
        List<Tasks> tasks = tasksRepositories.findAllByOrderByLevel(pageable);
        model.addAttribute("tabSize", tabSize);
        model.addAttribute("tasks", tasks);
        model.addAttribute("user", getUserData());
        model.addAttribute("themes", categoriesRepositories.findAllByOrderByCategory());
        return "task/tasks";
    }

    @GetMapping(path = "addNewCategory")
    @PreAuthorize("hasAuthority('ADMIN_ROLE')")
    public String addNewCategoryPage(Model model){
        model.addAttribute("themes", categoriesRepositories.findAllByOrderByCategory());
        return "task/addCategory";
    }

    @PostMapping(path = "addCategory")
    public String addCategory(Model model, @RequestParam(name = "name") String category){
        Optional<TaskCategories> taskCategories = categoriesRepositories.findByCategory(category);
        if(taskCategories.isPresent()){
            return "redirect:/task/addCategory";
        }
        TaskCategories categories = new TaskCategories(null, category);
        categoriesRepositories.save(categories);
        return "redirect:/task/tasks";
    }

//    @PostMapping(path = "deleteCategory")
//    @PreAuthorize("hasAuthority('MODERATOR_ROLE')")
//    public String deleteCategory(@RequestParam(name = "id") Long id){
//        Optional<TaskCategories> categories = categoriesRepositories.findById(id);
//        categoriesRepositories.delete(categories.get());
//        return "redirect:/task/addCategory";
//    }

    @GetMapping(path = "addNewTask")
    @PreAuthorize("hasAuthority('MODERATOR_ROLE')")
    public String addNewTaskPage(Model model){
        model.addAttribute("category", categoriesRepositories.findAllByOrderByCategory());
        return "task/addTasks";
    }

    private static  String uploadDirectory =  "C:\\Users\\Acer\\IdeaProjects\\3.2\\springSecurity\\src\\main\\resources\\static\\img\\";
    private static  String uploadDirectoryForTests =  "C:\\Users\\Acer\\IdeaProjects\\3.2\\springSecurity\\src\\main\\resources\\static\\tests\\";

    @PostMapping(path = "addTask")
    public String addTask(Model model, @RequestParam(name = "title") String title, @RequestParam(name = "description") String description,
                          @RequestParam(name = "input") String input, @RequestParam(name = "output") String output, @RequestParam(name = "category") Long category,
                          @RequestParam(name = "level") int level, @RequestParam(name = "img", required = false) MultipartFile multipartFile,
                          @RequestParam(name = "testInput") MultipartFile testInput, @RequestParam(name = "testOutput") MultipartFile testOutput){
        if(level != 0) {
            try {
                //Get the file and save it somewhere
                byte[] bytes = multipartFile.getBytes();
                String fileName = multipartFile.getOriginalFilename();
                if (multipartFile.isEmpty()) {
                    fileName = "task.png";
                }

                Path path = Paths.get(uploadDirectory + fileName);
                Files.write(path, bytes);
                System.out.println("img = " + fileName);

                Optional<TaskCategories> categories = categoriesRepositories.findById(category);

                Tasks saveTask = new Tasks(null, title, categories.get(), description, fileName, input, output, null, null, level, getUserData());
                tasksRepositories.save(saveTask);


                Optional<Tasks> task = tasksRepositories.findByTitleAndAuthor(title, getUserData());
                Long id = task.get().getId();
                File file = new File(uploadDirectoryForTests + "\\" + task.get().getAuthor().getId());
                file.mkdir();
                String newDir = uploadDirectoryForTests + "\\" + task.get().getAuthor().getId() + "\\";
                //INPUT file
                bytes = testInput.getBytes();
                String inputFile = id + "_INPUT.txt";
                path = Paths.get(newDir + inputFile);
                Files.write(path, bytes);
                //OUTPUT file
                bytes = testOutput.getBytes();
                String outputFile = id + "_OUTPUT.txt";
                path = Paths.get(newDir + outputFile);
                Files.write(path, bytes);

                task.get().setInputFile(inputFile);
                task.get().setOutputFile(outputFile);
                tasksRepositories.save(task.get());

                return "redirect:/task/tasks";
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return "redirect:/task/addTasks?error";
    }

    @PostMapping(path = "findByCategoryAndOrderByLevel")
    public String findByCategoryAndOrderByLevel(Model model, @RequestParam(name = "theme") String theme, @RequestParam(name = "level") String level,
                                                @RequestParam(name = "page", defaultValue = "1") int page){
        List<Tasks> tasksList;
        int size;

        Optional<TaskCategories> category = categoriesRepositories.findByCategory(theme);

        if(theme.isEmpty()) size = tasksRepositories.countAllBy();
        else size = tasksRepositories.countAllByCategories(category.get());

        int tabSize = (size+9)/10;
        if(tabSize < 1){
            tabSize = 1;
        }
        Pageable pageable = PageRequest.of(page-1,12);
        if(theme.isEmpty()){
            if(level.equals("asc")){
                tasksList = tasksRepositories.findAllByOrderByLevel(pageable);
            }
            else{
                tasksList = tasksRepositories.findAllByOrderByLevelDesc(pageable);
            }
        }else{
            if(level.equals("asc")){
                tasksList = tasksRepositories.findAllByCategoriesOrderByLevel(category.get(), pageable);
            }
            else{
                tasksList = tasksRepositories.findAllByCategoriesOrderByLevelDesc(category.get(), pageable);
            }
        }
        model.addAttribute("tabSize", tabSize);
        model.addAttribute("tasks", tasksList);
        model.addAttribute("user", getUserData());
        model.addAttribute("themes", categoriesRepositories.findAllByOrderByCategory());
        return "redirect:/task/tasks";
    }


    @PostMapping(path = "search")
    public String searchByTask(Model model, @RequestParam(name = "findTask") String task, @RequestParam(name = "page", defaultValue = "1") int page){
        int size = tasksRepositories.countAllByTitle(task);
        int tabSize = (size+9)/10;
        if(tabSize < 1){
            tabSize = 1;
        }
        Pageable pageable = PageRequest.of(page-1,12);
        List<Tasks> tasks = tasksRepositories.findAllByTitleOrderByLevel(task, pageable);
        model.addAttribute("tabSize", tabSize);
        model.addAttribute("tasks", tasks);
        model.addAttribute("user", getUserData());
        model.addAttribute("themes", categoriesRepositories.findAllByOrderByCategory());
        return "tasks";
    }

    @GetMapping(path = "/taskDetails/{id}")
    public String lessonDetailsPage(Model model, @PathVariable(name = "id") Long id){

        Tasks task= tasksRepositories.findById(id).get();

        model.addAttribute("task", task);
        Users user = getUserData();
        if(user != null && (user.getRoles().equals("ADMIN_ROLE") || task.getAuthor().equals(user))) {
            model.addAttribute("user", user);
        }

        return "task/taskDetails";
    }

    @GetMapping(path = "/editTask/{id}")
    @PreAuthorize("hasAuthority('MODERATOR_ROLE')")
    public String editPage(Model model, @PathVariable(name = "id") Long id){
        model.addAttribute("task", tasksRepositories.findById(id).get());
        model.addAttribute("categories", categoriesRepositories.findAllByOrderByCategory());
        return "task/editTask";
    }

    @PostMapping(path = "/editTask")
    public String editTask(Model model, @RequestParam(name = "title") String title, @RequestParam(name = "description") String description,
                           @RequestParam(name = "input") String input, @RequestParam(name = "output") String output, @RequestParam(name = "category") Long category,
                           @RequestParam(name = "level") int level, @RequestParam(name = "img", required = false) MultipartFile multipartFile,
                           @RequestParam(name = "testInput", required = false) MultipartFile testInput, @RequestParam(name = "testOutput", required = false) MultipartFile testOutput,
                           @RequestParam(name= "id") Long id){

        Optional<Tasks> tasks = tasksRepositories.findById(id);
        Tasks t = tasks.get();
        if(level != 0) {
            try {
                //Get the file and save it somewhere
                byte[] bytes = multipartFile.getBytes();
                String fileName = multipartFile.getOriginalFilename();
                if (multipartFile.isEmpty()) {
                    fileName = t.getFile();
                }
                Path path = Paths.get(uploadDirectory + fileName);
                Files.write(path, bytes);
                System.out.println("img = " + fileName);

                Optional<TaskCategories> categories = categoriesRepositories.findById(category);
                t.setFile(fileName);
                t.setCategories(categories.get());
                t.setInput(input);
                t.setOutput(output);
                t.setLevel(level);
                t.setDescription(description);
                t.setTitle(title);



                Optional<Tasks> task = tasksRepositories.findByTitleAndAuthor(title, getUserData());
                Long idTask = task.get().getId();

                String newDir = uploadDirectoryForTests + "\\" + task.get().getAuthor().getId() + "\\";

                //INPUT file
                bytes = testInput.getBytes();
                String inputFile = idTask + "_INPUT.txt";
                                                    //delete file
                File file = new File(newDir+inputFile);
                file.delete();
                path = Paths.get(newDir + inputFile);
                Files.write(path, bytes);
                //OUTPUT file
                bytes = testOutput.getBytes();
                String outputFile = idTask + "_OUTPUT.txt";
                                                    //delete file
                file = new File(newDir+outputFile);
                file.delete();
                path = Paths.get(newDir + outputFile);
                Files.write(path, bytes);

                t.setInputFile(inputFile);
                t.setOutputFile(outputFile);
                tasksRepositories.save(t);

                return "redirect:/task/tasks";
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return "redirect:/task/editTask?error";
    }
}
