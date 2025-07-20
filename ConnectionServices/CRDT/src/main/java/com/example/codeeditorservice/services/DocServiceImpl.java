package com.example.codeeditorservice.services;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import com.example.codeeditorservice.Dto.DocDTO;
import com.example.codeeditorservice.entities.Doc;
import com.example.codeeditorservice.entities.User;
import com.example.codeeditorservice.mapper.DocMapper;
import com.example.codeeditorservice.repository.DocRepository;
import com.example.codeeditorservice.repository.UserRepository;
import com.example.codeeditorservice.util.SecurityUtil;

@Service
