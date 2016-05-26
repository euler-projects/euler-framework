package net.eulerform.web.module.demo.controller;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Scope;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import net.eulerform.common.FileReader;
import net.eulerform.web.core.annotation.WebController;
import net.eulerform.web.core.base.controller.BaseController;
import net.eulerform.web.core.util.WebFileTool;
import net.eulerform.web.module.demo.entity.Blog;
import net.eulerform.web.module.demo.service.IBlogService;

@WebController
@Scope("prototype")
@RequestMapping("/demo")
public class DemoWebController extends BaseController {

	@Resource
	private IBlogService blogService;

	@RequestMapping(value = "/upload", method = RequestMethod.POST)
	public String upload(@RequestParam(value = "file", required = false) MultipartFile file, Model model) throws IllegalStateException, IOException {
		String uploadPath = this.getServletContext().getRealPath("upload");
		File savedFile = WebFileTool.saveMultipartFile(file, uploadPath);
		model.addAttribute("filename", savedFile.getPath());
		return "/demo/index";
	}

	@RequestMapping(value = "/loadBlog", method = RequestMethod.GET)
	public void test(HttpServletResponse response) throws IOException {
		String webInfPath = this.getServletContext().getRealPath("/WEB-INF/");
		String result = FileReader.readFileByLines(webInfPath + "/dev-resources/json/blogs.json");
		response.setContentType("application/json;charset=UTF-8");
		this.writeString(response, result);
	}

	@RequestMapping(value = "/loadBlogBody/{id}", method = RequestMethod.GET)
	public void test(HttpServletResponse response, @PathVariable("id") long id) throws IOException {
		String webInfPath = this.getServletContext().getRealPath("/WEB-INF/");
		String result = FileReader.readFileByLines(webInfPath + "/dev-resources/blogs/" + id + ".blog");
		response.setContentType("text/plain;charset=UTF-8");
		this.writeString(response, result);
	}

	@ResponseBody
	@RequestMapping(value = "/loadBlog/all", method = RequestMethod.GET)
	public List<Blog> findAllBlogs() {
		return this.blogService.findAllBlogs();
	}
}
