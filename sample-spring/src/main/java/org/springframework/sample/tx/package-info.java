/**
 * TX
 */
package org.springframework.sample.tx;

/*+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
											粗略关系图（可能有错漏）
+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

TransactionAttributeSource (TransactionAttribute 集)
		^
		| 通过 method 和 targetClass 从 TransactionAttributeSource 中获取 TransactionAttribute
   	   	|
TransactionAttribute (解析 @Transactional/<tx:method/> 得来)
		^
		| 根据 TransactionAttribute 确定 TransactionManager
   	   	|
TransactionManager (事务管理器)
		|--------------------------------------------------------------------
		|																	|
		| 使用 TransactionInfo 来保存事务信息									| 使用 TransactionStatus 来保存事务状态
		V																	V
TransactionInfo (事务信息)			---持有-->						TransactionStatus (事务状态)
+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++*/