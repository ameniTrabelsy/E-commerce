package com.codeWithProjects.ecom.services.customer.cart;

import com.codeWithProjects.ecom.dto.AddProductInCartDto;
import com.codeWithProjects.ecom.dto.CartItemsDto;
import com.codeWithProjects.ecom.dto.OrderDto;
import com.codeWithProjects.ecom.dto.PlaceOrderDto;
import com.codeWithProjects.ecom.entity.*;
import com.codeWithProjects.ecom.enums.OrderStatus;
import com.codeWithProjects.ecom.exception.ValidationException;
import com.codeWithProjects.ecom.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private CartItemsRepository cartItemsRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CouponRepository couponRepository;

   /* public ResponseEntity<?> addProductToCart(AddProductInCartDto addProductInCartDto) {
       Order activeOrder = orderRepository.findByUserIdAndOrderStatus(addProductInCartDto.getUserId(), OrderStatus.Pending);

        if (activeOrder == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("No active order found. Please start a new order.");
        }

        Optional<CartItems> optionalCartItems = cartItemsRepository.findByProductIdAndOrderIdAndUserId
                (addProductInCartDto.getProductId(), activeOrder.getId(), addProductInCartDto.getUserId());

        if(optionalCartItems.isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(null);
        }else{
            Optional<Product> optionalProduct = productRepository.findById(addProductInCartDto.getProductId());
            Optional<User> optionalUser = userRepository.findById(addProductInCartDto.getUserId());
             if( optionalProduct.isPresent() && optionalUser.isPresent()) {

                 CartItems cart = new CartItems();
                 cart.setProduct(optionalProduct.get());
                 cart.setPrice(Math.round(Double.parseDouble(optionalProduct.get().getPrice())));
                 cart.setQuantity(1L);
                 cart.setUser(optionalUser.get());
                 cart.setOrder(activeOrder);

                 CartItems updatedCart = cartItemsRepository.save(cart);

                 activeOrder.setTotalAmount(activeOrder.getTotalAmount() + cart.getPrice());
                 activeOrder.setAmount(activeOrder.getAmount() + cart.getPrice());
                 activeOrder.getCartItems().add(cart);

                 System.out.println("Cart price: " + cart.getPrice());
                 System.out.println("Amount before update: " + activeOrder.getAmount());
                 System.out.println("TotalAmount before update: " + activeOrder.getTotalAmount());


                 orderRepository.save(activeOrder);

                 return ResponseEntity.status(HttpStatus.CREATED).body(cart);

             }else {
                 return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User or product not found");
             }
        }
    }*/

    public ResponseEntity<?> addProductToCart(AddProductInCartDto addProductInCartDto) {

        // Vérifier si l'utilisateur existe
        Optional<User> optionalUser = userRepository.findById(addProductInCartDto.getUserId());
        if (!optionalUser.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
        User user = optionalUser.get();

        // Vérifier ou créer la commande active
        Order activeOrder = orderRepository.findByUserIdAndOrderStatus(user.getId(), OrderStatus.Pending);
        if (activeOrder == null) {
            activeOrder = new Order();
            activeOrder.setUser(user);
            activeOrder.setOrderStatus(OrderStatus.Pending);
            activeOrder.setDate(new Date());
            activeOrder.setAmount(0L);
            activeOrder.setTotalAmount(0L);
            activeOrder = orderRepository.save(activeOrder); // On sauvegarde pour avoir l'ID
        }

        // Vérifier si le produit existe
        Optional<Product> optionalProduct = productRepository.findById(addProductInCartDto.getProductId());
        if (!optionalProduct.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Product not found");
        }
        Product product = optionalProduct.get();

        // Vérifier si le produit est déjà dans le panier
        Optional<CartItems> optionalCartItems = cartItemsRepository.findByProductIdAndOrderIdAndUserId(
                product.getId(), activeOrder.getId(), user.getId()
        );

        if (optionalCartItems.isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Product already in cart");
        }

        // Créer et sauvegarder le nouvel item du panier
        CartItems cart = new CartItems();
        cart.setProduct(product);
        cart.setPrice((product.getPrice()));
        cart.setQuantity(1L);
        cart.setUser(user);
        cart.setOrder(activeOrder);

        cartItemsRepository.save(cart);

        // Mettre à jour le montant de la commande
        activeOrder.setTotalAmount(activeOrder.getTotalAmount() + cart.getPrice());
        activeOrder.setAmount(activeOrder.getAmount() + cart.getPrice());
        activeOrder.getCartItems().add(cart);

        orderRepository.save(activeOrder);

        return ResponseEntity.status(HttpStatus.CREATED).body(cart);
    }


    public OrderDto getCartByUserId(Long userId) {
        Order activeOrder = orderRepository.findByUserIdAndOrderStatus(userId, OrderStatus.Pending);

        OrderDto orderDto = new OrderDto();

        if (activeOrder != null) {
            // Remplir l’OrderDto avec les infos existantes
            List<CartItemsDto> cartItemsDtoList = activeOrder.getCartItems()
                    .stream()
                    .map(CartItems::getCartDto)
                    .collect(Collectors.toList());

            orderDto.setId(activeOrder.getId());
            orderDto.setAmount(activeOrder.getAmount());
            orderDto.setOrderStatus(activeOrder.getOrderStatus());
            orderDto.setDiscount(activeOrder.getDiscount());
            orderDto.setTotalAmount(activeOrder.getTotalAmount());
            orderDto.setCartItems(cartItemsDtoList);

            if (activeOrder.getCoupon() != null) {
                orderDto.setCouponName(activeOrder.getCoupon().getName());
            }
        } else {
            // Aucun panier existant → on peut initialiser un panier vide
            orderDto.setCartItems(new ArrayList<>());
            orderDto.setOrderStatus(OrderStatus.Pending);
            orderDto.setAmount(0L);
            orderDto.setTotalAmount(0L);
            orderDto.setDiscount(0L);
        }

        return orderDto;
    }


    public OrderDto applyCoupon(Long userId, String code){
        Order activeOrder = orderRepository.findByUserIdAndOrderStatus(userId, OrderStatus.Pending);
        Coupon coupon = couponRepository.findByCode(code).orElseThrow(() -> new ValidationException("Coupon not found"));

        if(couponIsExpired(coupon)){
            throw new ValidationException("Coupon has expired.");
        }

        double discountAmount = ((coupon.getDiscount() / 100.0) * activeOrder.getTotalAmount());
        double netAmount = activeOrder.getTotalAmount() - discountAmount;

        activeOrder.setAmount((long)netAmount);
        activeOrder.setDiscount((long)discountAmount);
        activeOrder.setCoupon(coupon);

        orderRepository.save(activeOrder);
        return activeOrder.getOrderDto();


    }

    private boolean couponIsExpired(Coupon coupon){
        Date currentDate = new Date();
        Date expirationDate = coupon.getExpirationDate();
        return expirationDate != null && currentDate.after(expirationDate);

    }

    public OrderDto increaseProductQuality(AddProductInCartDto addProductInCartDto) {
        Order activeOrder = orderRepository.findByUserIdAndOrderStatus(addProductInCartDto.getUserId(), OrderStatus.Pending);
        Optional<Product> optionalProduct = productRepository.findById(addProductInCartDto.getProductId());

        Optional<CartItems> optionalCartItems = cartItemsRepository.findByProductIdAndOrderIdAndUserId(
                addProductInCartDto.getProductId(), activeOrder.getId(), addProductInCartDto.getUserId()
        );

        if(optionalProduct.isPresent() && optionalCartItems.isPresent()) {
            CartItems cartItem = optionalCartItems.get();
            Product product = optionalProduct.get();

            activeOrder.setAmount(activeOrder.getAmount() + product.getPrice());
            activeOrder.setTotalAmount(activeOrder.getTotalAmount() + product.getPrice());

            cartItem.setQuantity(cartItem.getQuantity() + 1);

            if(activeOrder.getCoupon() != null) {
                double discountAmount = ((activeOrder.getCoupon().getDiscount() / 100.0) * activeOrder.getTotalAmount());
                double netAmount = activeOrder.getTotalAmount() - discountAmount;

                activeOrder.setAmount((long)netAmount);
                activeOrder.setDiscount((long)discountAmount);


            }
            cartItemsRepository.save(cartItem);
            orderRepository.save(activeOrder);
            return activeOrder.getOrderDto();
        }
        return null;


    }

    public OrderDto decreaseProductQuality(AddProductInCartDto addProductInCartDto) {
        Order activeOrder = orderRepository.findByUserIdAndOrderStatus(addProductInCartDto.getUserId(), OrderStatus.Pending);
        Optional<Product> optionalProduct = productRepository.findById(addProductInCartDto.getProductId());

        Optional<CartItems> optionalCartItems = cartItemsRepository.findByProductIdAndOrderIdAndUserId(
                addProductInCartDto.getProductId(), activeOrder.getId(), addProductInCartDto.getUserId()
        );

        if(optionalProduct.isPresent() && optionalCartItems.isPresent()) {
            CartItems cartItem = optionalCartItems.get();
            Product product = optionalProduct.get();

            activeOrder.setAmount(activeOrder.getAmount() - product.getPrice());
            activeOrder.setTotalAmount(activeOrder.getTotalAmount() - product.getPrice());

            cartItem.setQuantity(cartItem.getQuantity() - 1);

            if(activeOrder.getCoupon() != null) {
                double discountAmount = ((activeOrder.getCoupon().getDiscount() / 100.0) * activeOrder.getTotalAmount());
                double netAmount = activeOrder.getTotalAmount() - discountAmount;

                activeOrder.setAmount((long)netAmount);
                activeOrder.setDiscount((long)discountAmount);


            }
            cartItemsRepository.save(cartItem);
            orderRepository.save(activeOrder);
            return activeOrder.getOrderDto();
        }
        return null;


    }

    public OrderDto placeOrder(PlaceOrderDto placeOrderDto) {
        Order activeOrder = orderRepository.findByUserIdAndOrderStatus(placeOrderDto.getUserId(), OrderStatus.Pending);
        Optional<User> optionalUser = userRepository.findById(placeOrderDto.getUserId());
        if(optionalUser.isPresent()) {
            activeOrder.setOrderDescription(placeOrderDto.getOrderDescription());
            activeOrder.setAddress(placeOrderDto.getAddress());
            activeOrder.setDate(new Date());
            activeOrder.setOrderStatus(OrderStatus.Placed);
            activeOrder.setTrackingId(UUID.randomUUID());

            orderRepository.save(activeOrder);

            Order order = new Order();
            order.setAmount(0L);
            order.setTotalAmount(0L);
            order.setDiscount(0L);
            order.setUser(optionalUser.get());
            order.setOrderStatus(OrderStatus.Pending);
            orderRepository.save(order);

            return activeOrder.getOrderDto();

        }
        return null;

    }

    public List<OrderDto> getMyPlacedOrders(Long userId) {
        return orderRepository.findByUserIdAndOrderStatusIn(userId, List.of(OrderStatus.Placed, OrderStatus.Shipped,
                     OrderStatus.Delivered)).stream().map(Order::getOrderDto).collect(Collectors.toList());
    }

    public OrderDto searchOrderByTrackingId(UUID trackingId){
        Optional<Order> optionalOrder = orderRepository.findByTrackingId(trackingId);
        if(optionalOrder.isPresent()) {
            return optionalOrder.get().getOrderDto();
        }
        return null;
    }




}
