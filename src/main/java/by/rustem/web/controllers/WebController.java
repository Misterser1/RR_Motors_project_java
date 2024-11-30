package by.rustem.web.controllers;

import by.rustem.web.models.Post;
import by.rustem.web.repo.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

@Controller
public class WebController {
    private static final String UPLOAD_DIR = "src/main/resources/static/image_auto/";

    @Autowired
    private PostRepository postRepository;

    @GetMapping("/auto")
    public String autoMain(Model model) {
        Iterable<Post> posts = postRepository.findAll();
        model.addAttribute("posts", posts);
        return "auto-main";
    }

    @GetMapping("/auto/add")
    public String autoAdd(Model model) {
        return "auto-add";
    }

    @PostMapping("/auto/add")
    public String autoPostAdd(@RequestParam MultipartFile image, @RequestParam String title, @RequestParam String anons, @RequestParam String full_text, Model model){
        String imagePath = null;
        try {
            if(!image.isEmpty()) {
                byte[] bytes = image.getBytes();
                String uniqueFileName = System.currentTimeMillis() + "_" + image.getOriginalFilename();
                Path path = Paths.get(UPLOAD_DIR + uniqueFileName);
                Files.createDirectories(path.getParent());
                Files.write(path, bytes);
                imagePath = "/image_auto/" + uniqueFileName;
                System.out.println("Image Path: " + imagePath);
            }
        }catch (IOException e){
            e.printStackTrace();
            model.addAttribute("error", "Ошибка загрузки изображения!");
            return "auto-add";
        }

        Post post = new Post(title, anons, full_text);
        post.setImagePath(imagePath);
        postRepository.save(post);
        return "redirect:/auto";
    }

    @GetMapping("/auto/{id}")
    public String autoDetails(@PathVariable(value = "id") long id, Model model) {
        if(!postRepository.existsById(id)){
            return "redirect:/auto";
        }
        Optional<Post> post = postRepository.findById(id);
        ArrayList<Post> res = new ArrayList<>();
        post.ifPresent(res::add);
        Post currentPost = res.get(0);
        currentPost.setViews(currentPost.getViews() + 1);
        postRepository.save(currentPost);
        model.addAttribute("post", res);
        return "auto-details";
    }

    @GetMapping("/auto/{id}/edit")
    public String autoEdit(@PathVariable(value = "id") long id, Model model) {
        if(!postRepository.existsById(id)){
            return "redirect:/auto";
        }

        Optional<Post> post = postRepository.findById(id);
        ArrayList<Post> res = new ArrayList<>();
        post.ifPresent(res::add);
        model.addAttribute("post", res);
        return "auto-edit";
    }

    @PostMapping("/auto/{id}/edit")
    public String autoPostUpdate(@PathVariable(value = "id") long id, @RequestParam MultipartFile image, @RequestParam String title, @RequestParam String anons, @RequestParam String full_text, Model model){
        String imagePath = null;
        try {
            if(!image.isEmpty()) {
                byte[] bytes = image.getBytes();
                String uniqueFileName = System.currentTimeMillis() + "_" + image.getOriginalFilename();
                Path path = Paths.get(UPLOAD_DIR + uniqueFileName);
                Files.createDirectories(path.getParent());
                Files.write(path, bytes);
                imagePath = "/image_auto/" + uniqueFileName;
                System.out.println("Image Path: " + imagePath);
            }
        }catch (IOException e){
            e.printStackTrace();
            model.addAttribute("error", "Ошибка загрузки изображения!");
            return "auto-add";
        }

        Post post = postRepository.findById(id).orElseThrow();
        post.setTitle(title);
        post.setAnons(anons);
        post.setFull_text(full_text);
        post.setImagePath(imagePath);
        postRepository.save(post);

        return "redirect:/auto";
    }

    @PostMapping("/auto/{id}/remove")
    public String autoPostDelete(@PathVariable(value = "id") long id, Model model){
        Post post = postRepository.findById(id).orElseThrow();
        postRepository.delete(post);

        return "redirect:/auto";
    }
}
