package com.pierre.vendasonline.services;

import java.util.Date;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.pierre.vendasonline.domain.ItemPedido;
import com.pierre.vendasonline.domain.PagamentoComBoleto;
import com.pierre.vendasonline.domain.Pedido;
import com.pierre.vendasonline.domain.enums.EstadoPagamento;
import com.pierre.vendasonline.repositories.ItemPedidoRepository;
import com.pierre.vendasonline.repositories.PagamentoRepository;
import com.pierre.vendasonline.repositories.PedidoRepository;
import com.pierre.vendasonline.repositories.ProdutoRepository;
import com.pierre.vendasonline.services.exceptions.ObjectNotFoundException;

@Service
public class PedidoService {
	
	@Autowired
	private PedidoRepository repo;
	

	@Autowired
	private BoletoService boletoService;
	

	@Autowired
	private PagamentoRepository  pagamentoRepository;
	
	@Autowired
	private ItemPedidoRepository  itemPedidoRepository;
	
	

	@Autowired
	private ProdutoService  produtoService;
	
	
	public Pedido find(Integer id) {
		Optional<Pedido>  obj = repo.findById(id);
		return obj.orElseThrow(() -> new ObjectNotFoundException(
				"Objeto não encontrado! Id: " + id + ", Tipo: " + Pedido.class.getName()));
	}
	
	public Pedido insert(Pedido obj) {
		obj.setId(null);
		obj.setIstante(new Date());
		obj.getPagamento().setEstado(EstadoPagamento.PENDENTE);
		obj.getPagamento().setPedido(obj);
		if(obj.getPagamento() instanceof PagamentoComBoleto) {
			PagamentoComBoleto pagto = (PagamentoComBoleto) obj.getPagamento();
			boletoService.preencherPagamentoComBoleto(pagto, obj.getIstante());
		}
		
		obj = repo.save(obj);
		pagamentoRepository.save(obj.getPagamento());
		
		for(ItemPedido ip: obj.getItens()) {
			ip.setDesconto(0.0);
			ip.setPreco(produtoService.find(ip.getProduto().getId()).getPreco());
			ip.setPedido(obj);
		}
		itemPedidoRepository.saveAll(obj.getItens());
		return obj;
	}
	
}
