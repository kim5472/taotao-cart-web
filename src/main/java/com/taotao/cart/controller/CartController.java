package com.taotao.cart.controller;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.taotao.common.utils.CookieUtils;
import com.taotao.common.utils.JsonUtils;
import com.taotao.common.utils.TaotaoResult;
import com.taotao.pojo.TbItem;
import com.taotao.service.ItemService;

/**
 * 购物车管理Controller
 * 
 * @author kim
 *
 */
@Controller
public class CartController {
	
	@Value("${CART_KEY}")
	private String CART_KEY;
	
	@Value("${CART_EXPIRE}")
	private Integer CART_EXPIRE;
	
	@Autowired
	private ItemService itemService;
	
	@RequestMapping("/cart/add/{itemId}")
	@ResponseBody
	public String addItemCart(
			@PathVariable Long itemId,
			@RequestParam(defaultValue="1")Integer num,
			HttpServletRequest request,
			HttpServletResponse response){
	
		// 取购物车商品列表
		
		boolean flag = false;
		// 判断商品在购物车中是否存在
		List<TbItem> cartItemList = getCartItemList(request);
		for (TbItem tbItem : cartItemList) {
			if (tbItem.getId()==itemId.longValue()) {
				// 如果存在数量相加
				tbItem.setNum(tbItem.getNum()+num);
				flag = true;
				break;
			}
		}
		
		// 如果不存在，添加一个新的商品
		if (!flag) {
			// 需要调用服务取商品信息
			TbItem tbItem = itemService.getItemById(itemId);
			// 设置购买的商品数量
			tbItem.setNum(num);
			// 取一张图片
			String image = tbItem.getImage();
			if (StringUtils.isNotBlank(image)) {
				String[] images = image.split(",");
				tbItem.setImage(images[0]);
			}
			cartItemList.add(tbItem);
		}
		
		// 把购物车列表写入cookie
		CookieUtils.setCookie(
				request, response, CART_KEY, 
				JsonUtils.objectToJson(cartItemList), CART_EXPIRE, true);
		// 返回添加成功页面
		return "cartSuccess";
	}
	
	private List<TbItem> getCartItemList(HttpServletRequest request){
		// 从cookie中取购物车商品列表
		String json = CookieUtils.getCookieValue(request, CART_KEY, true);
		if (StringUtils.isBlank(json)) {
			// 如果没有值 返回一个空的list
			return new ArrayList<>();
		}
		List<TbItem> list = JsonUtils.jsonToList(json, TbItem.class);
		return list;
	}
	
}