package com.github.open.courier.management.service.biz;

import com.github.open.courier.management.application.service.ShardingTableAppService;
import com.github.open.courier.repository.mapper.MessageMapper;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class TableShardingBizServiceTest {

    @InjectMocks
    private ShardingTableAppService bizService;

    @Mock
    private MessageMapper messageMapper;

    @Test
    public void createShardingTable() {

        doNothing().when(messageMapper).createTable(anyString());

        bizService.createShardingTable("test", 2, 2,
                sharding -> messageMapper.createTable(sharding));

        verify(messageMapper, times(5)).createTable(anyString());
    }

    @Test
    @Ignore
    public void dropShardingTable() {

        doNothing().when(messageMapper).dropTable(anyString());

        List<String> allTables = new ArrayList<>();
        allTables.add("courier_message_20210530");
        allTables.add("courier_message_20210531");
        allTables.add("courier_message_20210601");
        allTables.add("courier_message_20210602");
        allTables.add("courier_message_20210603");
        allTables.add("courier_message_20210604");
        allTables.add("courier_message_20210605");
        allTables.add("courier_message_20210606");
        allTables.add("courier_message_20210607");
        when(messageMapper.listTableNames("courier_message")).thenReturn(allTables);

        bizService.dropShardingTable("courier_message", 2,
                table -> messageMapper.listTableNames(table),
                table -> messageMapper.dropTable(table));

        verify(messageMapper, times(7)).dropTable(anyString());

    }

}