package com.zeyigou.sellergoods.service.impl;
import java.util.List;

import com.zeyigou.mapper.TbItemCatMapper;
import com.zeyigou.pojo.PageResult;
import com.zeyigou.pojo.TbItemCat;
import com.zeyigou.pojo.TbItemCatExample;
import com.zeyigou.sellergoods.service.ItemCatService;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.data.redis.core.RedisTemplate;


/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
public class ItemCatServiceImpl implements ItemCatService {

	@Autowired
	private TbItemCatMapper itemCatMapper;
	@Autowired
	private RedisTemplate redisTemplate;
	
	/**
	 * 查询全部
	 */
	@Override
	public List<TbItemCat> findAll() {
		return itemCatMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbItemCat> page=   (Page<TbItemCat>) itemCatMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(TbItemCat itemCat) {
		itemCatMapper.insert(itemCat);		
	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(TbItemCat itemCat){
		itemCatMapper.updateByPrimaryKey(itemCat);
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public TbItemCat findOne(Long id){
		return itemCatMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			itemCatMapper.deleteByPrimaryKey(id);
		}		
	}
	
	
		@Override
	public PageResult findPage(TbItemCat itemCat, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbItemCatExample example=new TbItemCatExample();
		TbItemCatExample.Criteria criteria = example.createCriteria();
		
		if(itemCat!=null){			
						if(itemCat.getName()!=null && itemCat.getName().length()>0){
				criteria.andNameLike("%"+itemCat.getName()+"%");
			}
	
		}
		
		Page<TbItemCat> page= (Page<TbItemCat>)itemCatMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 根据父id查询子分类列表
	 * @param parentId
	 * @return
	 */
	@Override
	public List<TbItemCat> findByParentId(Long parentId) {

		//0.将分类名称及模板id放到redis中
		//0.1)得到所有分类列表
		List<TbItemCat> itemCats = findAll();
		//0.2)遍历分类列表并以分类名称为key，以模板id为值放到redids中
		for (TbItemCat itemCat : itemCats) {
			redisTemplate.boundHashOps("itemCatList").put(itemCat.getName(),itemCat.getTypeId());
		}

		//1.定义查询实例
		TbItemCatExample example = new TbItemCatExample();
		//2.添加查询条件
		TbItemCatExample.Criteria criteria = example.createCriteria();
		criteria.andParentIdEqualTo(parentId);
		//3.进行条件查询（外键：父id）
		List<TbItemCat> tbItemCats = itemCatMapper.selectByExample(example);
		//4.返回列表
		return tbItemCats;
	}

}
