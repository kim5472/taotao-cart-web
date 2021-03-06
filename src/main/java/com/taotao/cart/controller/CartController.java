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
import org.springframework.web.bind.annotation.RequestMethod;
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
	
	/**
	 * 显示购物车列表
	 * @param request
	 * @return
	 */
	@RequestMapping("/cart/cart")
	public String showCartList(HttpServletRequest request){
		// 从cookie中取购物车列表
		List<TbItem> cartItemList = getCartItemList(request);
		// 把购物车列表传递给jsp
		request.setAttribute("cartList", cartItemList);
		// 返回逻辑视图
		return "cart";
	}
	
	/**
	 * 购物车商品更新数量
	 * @param itemId 商品id
	 * @param num 修改的商品数量
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value="/cart/update/num/{itemId}/{num}",method=RequestMethod.POST)
	@ResponseBody
	public TaotaoResult updateItemNum(
			@PathVariable Long itemId,
			@PathVariable Integer num,
			HttpServletRequest request,
			HttpServletResponse response){
		// 从cookie中获取购物车列表
		List<TbItem> cartItemList = getCartItemList(request);
		// 查询到对应的商品
		for (TbItem tbItem : cartItemList) {
			if (tbItem.getId()==itemId.longValue()) {
				// 更新商品数量
				tbItem.setNum(num);
				break;
			}
		}
		// 把购物车列表写入cookie
		CookieUtils.setCookie(
				request, response, CART_KEY, 
				JsonUtils.objectToJson(cartItemList), CART_EXPIRE, true);
		// 返回成功
		return TaotaoResult.ok();
	}
	
	/**
	 * 购物车删除一项
	 * @param itemId 商品id
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping("/cart/delete/{itemId}")
	public String deleteCartItem(
			@PathVariable Long itemId,
			HttpServletRequest request,
			HttpServletResponse response){
		// 从cookie中取出购物车列表
		List<TbItem> cartItemList = getCartItemList(request);
		// 找到对应的商品
		for (TbItem tbItem : cartItemList) {
			if (tbItem.getId()==itemId.longValue()) {
				// 删除商品
				cartItemList.remove(tbItem);// 可能出现问题，应该用底层删除
				break;
			}
		}
		// 把购物车列表重写入cookie
		CookieUtils.setCookie(
				request, response, CART_KEY, 
				JsonUtils.objectToJson(cartItemList), CART_EXPIRE, true);
		// 重定向
		return "redirect:/cart/cart.html";
	}
	
	
	
}
