package com.zeyigou.sellergoods.service.impl;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.zeyigou.group.Goods;
import com.zeyigou.mapper.*;
import com.zeyigou.pojo.*;
import com.zeyigou.sellergoods.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.transaction.annotation.Transactional;


/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
@Transactional
public class GoodsServiceImpl implements GoodsService {

	@Autowired
	private TbGoodsMapper goodsMapper;
	@Autowired
	private TbGoodsDescMapper goodsDescMapper;
	@Autowired
	private TbItemMapper itemMapper;
	@Autowired
	private TbBrandMapper brandMapper;
	@Autowired
	private TbItemCatMapper itemCatMapper;
	@Autowired
	private TbSellerMapper sellerMapper;


	/**
	 * 查询全部
	 */
	@Override
	public List<TbGoods> findAll() {
		return goodsMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbGoods> page=   (Page<TbGoods>) goodsMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(Goods goods) {
		//1.添加商品
		goods.getGoods().setAuditStatus("0");	//未审核
		goodsMapper.insert(goods.getGoods());


		//2.设置goodsdesc表的主键，与goods表的主键一致
		goods.getGoodsDesc().setGoodsId(goods.getGoods().getId());
		//2.1) 添加商品描述
		goodsDescMapper.insert(goods.getGoodsDesc());

		//3.添加sku商品列表
		addItem(goods);

	}

	private void addItem(Goods goods) {
		//3.1)得到sku商品列表
		List<TbItem> items = goods.getItems();
		//3.2)遍历sku
		for (TbItem item : items) {
			//3.2.1)设置标题
			item.setTitle( goods.getGoods().getGoodsName());
			//3.2.2)设置分类id
			item.setCategoryid(goods.getGoods().getCategory3Id());
			//3.2.3)设置创建时间与修改时间
			item.setCreateTime(new Date());
			item.setUpdateTime(new Date());
			//3.2.4)设置商家id
			item.setSellerId(goods.getGoods().getSellerId());
			//3.2.5)设置商品id
			item.setGoodsId(goods.getGoods().getId());
			//3.2.6)设置状态值（正常）
			item.setStatus("1");
			//3.2.7)查询品牌名称
			Long brandId = goods.getGoods().getBrandId();
			String brandName = brandMapper.selectByPrimaryKey(brandId).getName();
			item.setBrand(brandName);
			//3.2.8)得到分类名称
			String cateName = itemCatMapper.selectByPrimaryKey(goods.getGoods().getCategory3Id()).getName();
			item.setCategory(cateName);
			//3.2.9)得到商家名称
			String sellerName = sellerMapper.selectByPrimaryKey(goods.getGoods().getSellerId()).getName();
			item.setSeller(sellerName);

			//3.2.10)得到上传的所有图片列表
			String itemImages = goods.getGoodsDesc().getItemImages();
			List<Map> maps = JSON.parseArray(itemImages, Map.class);
			//3.2.11)设置这个sku商品的图片
			if(maps != null && maps.size() > 0){
				item.setImage(maps.get(0).get("url")+"");
			}
			//3.2.12)添加sku到数据库中
			itemMapper.insert(item);
		}
	}


	/**
	 * 修改
	 */
	@Override
	public void update(Goods goods){
		//1.修改商品表的数据
		goodsMapper.updateByPrimaryKey(goods.getGoods());

		//2.修改商品描述表的数据
		goodsDescMapper.updateByPrimaryKey(goods.getGoodsDesc());

		//3.对于sku商品列表来说，可以先删除再添加
		//3.1)根据外键来删除sku商品列表的数据
		TbItemExample example = new TbItemExample();
		TbItemExample.Criteria criteria = example.createCriteria();
		criteria.andGoodsIdEqualTo(goods.getGoods().getId());
		itemMapper.deleteByExample(example);

		//3.2）重新在tb_item表中添加新sku列表
		addItem(goods);

	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public Goods findOne(Long id){
		//1.查询得到goods表的数据
		TbGoods tbGoods = goodsMapper.selectByPrimaryKey(id);

		//2.查询得到goodsDesc表的数据
		TbGoodsDesc tbGoodsDesc = goodsDescMapper.selectByPrimaryKey(id);

		//3.查询得到sku商品列表
		TbItemExample example = new TbItemExample();
		TbItemExample.Criteria criteria = example.createCriteria();
		criteria.andGoodsIdEqualTo(id);
		List<TbItem> items = itemMapper.selectByExample(example);

		//4.定义组合对象
		Goods goods = new Goods();
		goods.setGoods(tbGoods);
		goods.setGoodsDesc(tbGoodsDesc);
		goods.setItems(items);

		//5.返回
		return goods;
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			//1.查询商品
			TbGoods tbGoods = goodsMapper.selectByPrimaryKey(id);
			tbGoods.setIsDelete("1");
			//2.修改商品
			goodsMapper.updateByPrimaryKey(tbGoods);
		}		
	}
	
	
		@Override
	public PageResult findPage(TbGoods goods, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbGoodsExample example=new TbGoodsExample();
		TbGoodsExample.Criteria criteria = example.createCriteria();
		
		if(goods!=null){			
						if(goods.getSellerId()!=null && goods.getSellerId().length()>0){
				criteria.andSellerIdLike("%"+goods.getSellerId()+"%");
			}
			if(goods.getGoodsName()!=null && goods.getGoodsName().length()>0){
				criteria.andGoodsNameLike("%"+goods.getGoodsName()+"%");
			}
			if(goods.getAuditStatus()!=null && goods.getAuditStatus().length()>0){
				criteria.andAuditStatusLike("%"+goods.getAuditStatus()+"%");
			}
			if(goods.getIsMarketable()!=null && goods.getIsMarketable().length()>0){
				criteria.andIsMarketableLike("%"+goods.getIsMarketable()+"%");
			}
			if(goods.getCaption()!=null && goods.getCaption().length()>0){
				criteria.andCaptionLike("%"+goods.getCaption()+"%");
			}
			if(goods.getSmallPic()!=null && goods.getSmallPic().length()>0){
				criteria.andSmallPicLike("%"+goods.getSmallPic()+"%");
			}
			if(goods.getIsEnableSpec()!=null && goods.getIsEnableSpec().length()>0){
				criteria.andIsEnableSpecLike("%"+goods.getIsEnableSpec()+"%");
			}
			if(goods.getIsDelete()!=null && goods.getIsDelete().length()>0){
				criteria.andIsDeleteLike("%"+goods.getIsDelete()+"%");
			}
	
		}
		
		Page<TbGoods> page= (Page<TbGoods>)goodsMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}

	//商品审核
	@Override
	public void updateStatus(String status, Long[] ids) {
		for (Long id : ids) {
			//1.根据主键查询商品
			TbGoods tbGoods = goodsMapper.selectByPrimaryKey(id);
			tbGoods.setAuditStatus(status);
			//2.修改商品
			goodsMapper.updateByPrimaryKey(tbGoods);
		}
	}
}
